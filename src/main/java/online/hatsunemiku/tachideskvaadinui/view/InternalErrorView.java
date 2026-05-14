/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.view.source.SourcesView;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@code InternalErrorView} class provides a high-tech, Hatsune Miku themed error page
 * matching the Stitch design. It includes a navigation top bar and a dual-button error card.
 */
@Tag("div")
@CssImport("./css/views/internal-error.css")
@Route("error")
public class InternalErrorView extends Div implements HasErrorParameter<Exception> {

    private final Span errorCodeBadge;
    private final H2 errorTitle;
    private final Span errorMessage;
    private final Div searchDropdown;
    private final MangaService mangaService;
    private final SettingsService settingsService;

    @Autowired
    public InternalErrorView(MangaService mangaService, SettingsService settingsService) {
        this.mangaService = mangaService;
        this.settingsService = settingsService;
        setId("internal-error-view");

        // Top Bar
        Div topBar = new Div();
        topBar.setClassName("top-bar");

        Div navLinks = new Div();
        navLinks.setClassName("nav-links");
        
        Span libraryLink = new Span("Library");
        libraryLink.setClassName("nav-link active");
        libraryLink.addClickListener(e -> UI.getCurrent().navigate(RootView.class));
        
        Span discoverLink = new Span("Discover");
        discoverLink.setClassName("nav-link");
        discoverLink.addClickListener(e -> UI.getCurrent().navigate(SourcesView.class));
        
        navLinks.add(libraryLink, discoverLink);

        Div searchContainer = new Div();
        searchContainer.setClassName("search-container");
        
        TextField searchInput = new TextField();
        searchInput.setPlaceholder("Search library...");
        searchInput.setClassName("search-input");
        searchInput.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchInput.setValueChangeMode(ValueChangeMode.EAGER);
        searchInput.addValueChangeListener(e -> updateSearchDropdown(e.getValue()));
        
        searchDropdown = new Div();
        searchDropdown.setClassName("search-dropdown");
        searchDropdown.setVisible(false);
        
        searchContainer.add(searchInput, searchDropdown);

        topBar.add(navLinks, searchContainer);
        add(topBar);

        // Content
        Div errorContent = new Div();
        errorContent.setClassName("error-content");

        Div glitchOverlay = new Div();
        glitchOverlay.setClassName("glitch-overlay");
        errorContent.add(glitchOverlay);

        Div errorCard = new Div();
        errorCard.setClassName("error-card");

        Div iconWrapper = new Div();
        iconWrapper.setClassName("status-icon-wrapper");
        iconWrapper.add(VaadinIcon.REFRESH.create()); // Sync arrows
        iconWrapper.getChildren().forEach(c -> c.setClassName("status-icon"));

        errorTitle = new H2("Page Load Failed");
        errorTitle.setClassName("error-title");

        errorCodeBadge = new Span("ERROR CODE: SCR_SYNC_TIMEOUT");
        errorCodeBadge.setClassName("error-badge");

        errorMessage = new Span("The manga synchronization server timed out due to heavy traffic or network disruption.");
        errorMessage.setClassName("error-description");

        Div buttonGroup = new Div();
        buttonGroup.setClassName("button-group");

        Div libButton = new Div();
        libButton.setClassName("stitch-btn btn-primary");
        libButton.add(VaadinIcon.GRID_BIG_O.create());
        libButton.add(new Span("Back to Library"));
        libButton.addClickListener(e -> UI.getCurrent().navigate(RootView.class));

        Div settingsButton = new Div();
        settingsButton.setClassName("stitch-btn btn-secondary");
        settingsButton.add(VaadinIcon.COG.create());
        settingsButton.add(new Span("Settings"));
        settingsButton.addClickListener(e -> UI.getCurrent().navigate(SettingsView.class));

        buttonGroup.add(libButton, settingsButton);

        errorCard.add(iconWrapper, errorTitle, errorCodeBadge, errorMessage, buttonGroup);
        errorContent.add(errorCard);
        add(errorContent);

        // Footer Info
        Div footerInfo = new Div();
        footerInfo.setText("VaaUI Error Handler");
        footerInfo.setClassName("error-footer-info");
        add(footerInfo);
    }

    private void updateSearchDropdown(String query) {
        if (query == null || query.trim().length() < 2) {
            searchDropdown.setVisible(false);
            return;
        }

        searchDropdown.removeAll();
        List<Manga> filtered;
        try {
            List<Manga> library = mangaService.getLibraryManga();
            filtered = library.stream()
                    .filter(m -> m.getTitle().toLowerCase().contains(query.toLowerCase()))
                    .limit(5)
                    .toList();
        } catch (Exception e) {
            Div error = new Div();
            error.setText("Unable to reach library server");
            error.setClassName("dropdown-no-results");
            searchDropdown.add(error);
            searchDropdown.setVisible(true);
            return;
        }
        
        if (filtered.isEmpty()) {
            Div noResults = new Div();
            noResults.setText("No library matches found");
            noResults.setClassName("dropdown-no-results");
            searchDropdown.add(noResults);
        } else {
            Div header = new Div();
            header.setText("SEARCH RESULTS");
            header.setClassName("dropdown-header");
            searchDropdown.add(header);

            Settings settings = settingsService.getSettings();
            for (Manga manga : filtered) {
                searchDropdown.add(createDropdownItem(manga, settings));
            }

            Anchor viewAll = new Anchor("/search/" + query, "View all results for '" + query + "'");
            viewAll.setClassName("dropdown-footer");
            searchDropdown.add(viewAll);
        }

        searchDropdown.setVisible(true);
    }

    private Div createDropdownItem(Manga manga, Settings settings) {
        Div item = new Div();
        item.setClassName("dropdown-item");
        item.addClickListener(e -> UI.getCurrent().navigate("manga/" + manga.getId()));

        Image thumbnail = new Image(settings.getUrl() + manga.getThumbnailUrl(), manga.getTitle());
        thumbnail.setClassName("dropdown-item-thumb");

        Div info = new Div();
        info.setClassName("dropdown-item-info");

        Span title = new Span(manga.getTitle());
        title.setClassName("dropdown-item-title");

        String authorArtist = manga.getAuthor();
        if (manga.getArtist() != null && !manga.getArtist().equals(authorArtist)) {
            authorArtist += " / " + manga.getArtist();
        }
        Span meta = new Span((authorArtist != null ? authorArtist : "Unknown") + " • " + (manga.getStatus() != null ? manga.getStatus() : "Unknown"));
        meta.setClassName("dropdown-item-meta");

        info.add(title, meta);
        item.add(thumbnail, info);
        return item;
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        if (parameter.getException() instanceof NotFoundException) {
            statusCode = HttpServletResponse.SC_NOT_FOUND;
            errorTitle.setText("Resource Not Found");
            errorCodeBadge.setText("ERROR CODE: 404_NOT_FOUND");
            errorMessage.setText("The requested digital resource does not exist.");
        } else {
            errorTitle.setText("Page Load Failed");
            errorCodeBadge.setText("ERROR CODE: 500_INTERNAL_GLITCH");
            
            String details = parameter.getException() != null ? parameter.getException().getMessage() : "Unknown sync error";
            errorMessage.setText("The server encountered an unhandled exception: " + details);
        }

        return statusCode;
    }
}
