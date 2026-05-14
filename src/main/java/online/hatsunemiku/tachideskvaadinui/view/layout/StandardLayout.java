/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.layout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.notification.WebPushService;
import online.hatsunemiku.tachideskvaadinui.view.ExtensionsView;
import online.hatsunemiku.tachideskvaadinui.view.RootView;
import online.hatsunemiku.tachideskvaadinui.view.SearchView;
import online.hatsunemiku.tachideskvaadinui.view.SettingsView;
import online.hatsunemiku.tachideskvaadinui.view.source.SourceExploreView;
import online.hatsunemiku.tachideskvaadinui.view.source.SourcesView;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A standard layout used by most views in the application. It contains a navigation bar, the main
 * content and a footer.
 *
 * @version 1.12.0
 * @since 0.9.0
 */
@Slf4j
@CssImport("./css/common.css")
public class StandardLayout extends VerticalLayout {

  private final VerticalLayout content;
  private final Footer footer;
  private Div searchDropdown;
  private HorizontalLayout navBar;
  @Autowired private WebPushService webPushService;
  @Autowired private MangaService mangaService;
  @Autowired private SettingsService settingsService;

  public StandardLayout(String title) {
    setId("container");
    setSizeFull();
    setPadding(false);
    setSpacing(false);
    getNavBar(title);

    content = new VerticalLayout();
    content.setClassName("content");
    content.setWidthFull();
    content.setPadding(false);
    content.setSpacing(false);
    setFlexGrow(1, content);

    var footer = getFooter();
    this.footer = footer;

    this.add(navBar, content, footer);
  }

  private void getNavBar(String title) {
    navBar = new HorizontalLayout();
    navBar.setClassName("global-navbar");
    navBar.setWidthFull();
    navBar.setSpacing(false);
    navBar.setPadding(false);

    Div primaryNav = new Div();
    primaryNav.setClassName("primary-nav");

    addRootBtn(primaryNav);
    addExtensionsBtn(primaryNav);
    addSourcesBtn(primaryNav);

    Div actionNav = new Div();
    actionNav.setClassName("action-nav");

    addSettingsBtn(actionNav);
    addLibrarySearch(actionNav);

    navBar.add(primaryNav, actionNav);
    navBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    navBar.setAlignItems(FlexComponent.Alignment.CENTER);
  }

  public void addSettingsBtn(Div container) {
    Button settingsButton = new Button(VaadinIcon.COG.create());
    if (this instanceof SettingsView) settingsButton.addClassName("active-nav");
    settingsButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SettingsView.class)));
    addBtn(container, settingsButton);
  }

  private void addLibrarySearch(Div container) {
    Div searchContainer = new Div();
    searchContainer.setClassName("global-search-container");

    TextField searchInput = new TextField();
    searchInput.setPlaceholder("Search library...");
    searchInput.setClassName("global-search-input");
    searchInput.setPrefixComponent(VaadinIcon.SEARCH.create());
    searchInput.setValueChangeMode(ValueChangeMode.EAGER);
    searchInput.setClearButtonVisible(true);
    
    searchInput.addValueChangeListener(e -> updateSearchDropdown(e.getValue()));
    
    searchInput.addFocusListener(e -> {
        if (searchInput.getValue() != null && searchInput.getValue().trim().length() >= 2) {
            searchDropdown.setVisible(true);
        }
    });

    searchInput.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> {
        if (!searchInput.isEmpty()) {
            getUI().ifPresent(ui -> ui.navigate(SearchView.class, searchInput.getValue()));
            searchDropdown.setVisible(false);
        }
    });

    searchDropdown = new Div();
    searchDropdown.setClassName("global-search-dropdown");
    searchDropdown.setVisible(false);

    searchContainer.add(searchInput, searchDropdown);
    container.add(searchContainer);
    
    // Hide dropdown on click outside (simple approach)
    searchInput.getElement().executeJs(
        "document.addEventListener('click', (e) => {" +
        "  if (!$0.contains(e.target)) { $1.style.display = 'none'; }" +
        "});", searchContainer.getElement(), searchDropdown.getElement());
  }

  private void updateSearchDropdown(String query) {
    if (searchDropdown == null) {
      return;
    }

    if (query == null || query.trim().length() < 2) {
      searchDropdown.setVisible(false);
      return;
    }

    searchDropdown.removeAll();
    String trimmedQuery = query.trim();

    List<Manga> filtered;
    try {
      if (mangaService == null) {
        addSearchPageFooter(trimmedQuery);
        searchDropdown.getStyle().set("display", "flex");
        searchDropdown.setVisible(true);
        return;
      }

      List<Manga> library = mangaService.getLibraryManga();
      String queryLower = trimmedQuery.toLowerCase();
      filtered =
          library.stream()
              .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase().contains(queryLower))
              .limit(5)
              .toList();
    } catch (Exception e) {
      Div error = new Div();
      error.setText("Unable to reach library server");
      error.setClassName("global-dropdown-no-results");
      searchDropdown.add(error);
      addSearchPageFooter(trimmedQuery);
      searchDropdown.getStyle().set("display", "flex");
      searchDropdown.setVisible(true);
      return;
    }

    if (filtered.isEmpty()) {
      Div noResults = new Div();
      noResults.setText("No library matches found");
      noResults.setClassName("global-dropdown-no-results");
      searchDropdown.add(noResults);
    } else {
      Div header = new Div();
      header.setText("SEARCH RESULTS");
      header.setClassName("global-dropdown-header");
      searchDropdown.add(header);

      Settings settings = settingsService != null ? settingsService.getSettings() : null;
      for (Manga manga : filtered) {
        searchDropdown.add(createDropdownItem(manga, settings));
      }
    }

    addSearchPageFooter(trimmedQuery);
    searchDropdown.getStyle().set("display", "flex");
    searchDropdown.setVisible(true);
  }

  private void addSearchPageFooter(String query) {
    Div footerLink = new Div();
    footerLink.setText("View all results for '" + query + "'");
    footerLink.setClassName("global-dropdown-footer");
    footerLink.addClickListener(e -> {
        getUI().ifPresent(ui -> ui.navigate(SearchView.class, query));
        searchDropdown.setVisible(false);
    });
    searchDropdown.add(footerLink);
  }

  private Div createDropdownItem(Manga manga, Settings settings) {
    Div item = new Div();
    item.setClassName("global-dropdown-item");
    item.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("manga/" + manga.getId())));

    String thumbnailSrc =
        settings == null ? "" : settings.getUrl() + (manga.getThumbnailUrl() == null ? "" : manga.getThumbnailUrl());
    Image thumbnail = new Image(thumbnailSrc, manga.getTitle());
    thumbnail.setClassName("global-dropdown-item-thumb");

    Div info = new Div();
    info.setClassName("global-dropdown-item-info");

    Span title = new Span(manga.getTitle());
    title.setClassName("global-dropdown-item-title");

    String authorArtist = manga.getAuthor();
    if (manga.getArtist() != null && !manga.getArtist().equals(authorArtist)) {
      authorArtist += " / " + manga.getArtist();
    }

    String metaText =
        (authorArtist != null ? authorArtist : "Unknown")
            + " • "
            + (manga.getStatus() != null ? manga.getStatus() : "Unknown");
    Span meta = new Span(metaText);
    meta.setClassName("global-dropdown-item-meta");

    info.add(title, meta);
    item.add(thumbnail, info);
    return item;
  }

  private void addSourcesBtn(Div container) {
    Button sourcesButton = new Button("Discover", VaadinIcon.GLOBE.create());
    if (this instanceof SourcesView || this instanceof SourceExploreView) {
      sourcesButton.addClassName("active-nav");
    }
    sourcesButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SourcesView.class)));
    addBtn(container, sourcesButton);
  }

  private void addRootBtn(Div container) {
    Button rootButton = new Button("Library", VaadinIcon.BOOK.create());
    if (this instanceof RootView) rootButton.addClassName("active-nav");
    rootButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(RootView.class)));
    addBtn(container, rootButton);
  }

  private void addExtensionsBtn(Div container) {
    Button extensionsButton = new Button("Extensions", VaadinIcon.PUZZLE_PIECE.create());
    if (this instanceof ExtensionsView) extensionsButton.addClassName("active-nav");
    extensionsButton.addClickListener(
        e -> getUI().ifPresent(ui -> ui.navigate(ExtensionsView.class)));
    addBtn(container, extensionsButton);
  }

  private void addBtn(Div container, Button btn) {
    btn.addClassName("nav-btn");
    container.add(btn);
  }

  @NotNull
  private Footer getFooter() {
    Footer footer = new Footer();

    int copyrightYear = LocalDate.now().getYear();
    String copyright = "© %d Alessandro Schwaiger".formatted(copyrightYear);

    footer.add(copyright);
    return footer;
  }

  protected void setContent(Component content) {
    this.content.removeAll();
    this.content.add(content);
  }

  protected void fullScreen() {
    this.content.setClassName("content-fullscreen");
    this.navBar.setVisible(false);
    this.footer.setVisible(false);
    addClassName("fullscreen");
  }

  protected void fullScreenNoHide() {
    this.content.setClassName("content-fullscreen");
    addClassName("fullscreen");
  }

  /**
   * Sets the UI to windowed mode by updating the class names and visibility of different
   * components. This method is only supposed to be called when {@link #fullScreen()} was called
   * before.
   */
  protected void windowed() {
    this.content.setClassName("content");
    this.navBar.setVisible(true);
    this.footer.setVisible(true);
    removeClassName("fullscreen");
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    if (webPushService == null) {
      log.warn("WebPushService is null");
      return;
    }

    UI ui = attachEvent.getUI();
    webPushService.checkExistingSubscription(ui);
  }
}
