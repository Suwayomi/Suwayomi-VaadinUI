/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.services.ExtensionService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import online.hatsunemiku.tachideskvaadinui.view.source.SourcesView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("extensions")
@CssImport("./css/views/extensions.css")
public class ExtensionsView extends StandardLayout {

  private static final Logger log = LoggerFactory.getLogger(ExtensionsView.class);
  private final ExtensionService extensionService;
  private Settings settings;
  private final Div cardsGrid = new Div();
  private final Span pendingTitle = new Span();
  private final Span pendingDescription = new Span();
  private final Button updateAllButton = new Button("Update All");
  private final Span installedTab = new Span();
  private final Span availableTab = new Span();
  private final Button loadMoreButton = new Button("Load More");
  private TextField searchField;
  private boolean showingInstalled = true;
  private int visibleCards = 6;
  private static final int PAGE_SIZE = 6;
  private List<Extension> extensions = new ArrayList<>();

  public ExtensionsView(ExtensionService extensionService, SettingsService settingsService) {
    super("Extension Manager");
    this.extensionService = extensionService;
    setClassName("extensions-view");

    try {
      this.extensions = extensionService.getExtensions();
    } catch (Exception e) {
      redirectToSettings(e);
      return;
    }

    this.settings = settingsService.getSettings();
    Div shell = new Div();
    shell.setClassName("extension-manager-shell");

    shell.add(createHeroSection(), createUpdatesBanner(), createFilterTabs(), cardsGrid, createLoadMoreSection());

    setContent(shell);
    searchField.setValue("");
    render();
  }

  private Div createHeroSection() {
    Div hero = new Div();
    hero.setClassName("manager-hero");

    Div left = new Div();
    left.setClassName("manager-hero-left");
    H2 title = new H2("Extension Manager");
    Span subtitle = new Span("Manage sources and synchronization engines for your library.");
    subtitle.setClassName("manager-subtitle");
    left.add(title, subtitle);

    Div right = new Div();
    right.setClassName("manager-hero-right");
    searchField = new TextField();
    searchField.setPlaceholder("Search extensions...");
    searchField.addClassName("manager-search");
    searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
    searchField.setValueChangeMode(ValueChangeMode.EAGER);
    searchField.addValueChangeListener(
        e -> {
          visibleCards = PAGE_SIZE;
          render();
        });

    Button browseSources = new Button("Browse Sources", VaadinIcon.PLUS_CIRCLE_O.create());
    browseSources.setClassName("browse-btn");
    browseSources.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SourcesView.class)));
    right.add(searchField, browseSources);

    hero.add(left, right);
    return hero;
  }

  private Div createUpdatesBanner() {
    Div banner = new Div();
    banner.setClassName("updates-banner");

    Div iconWrap = new Div(VaadinIcon.BOLT.create());
    iconWrap.setClassName("updates-icon");

    Div textWrap = new Div();
    textWrap.setClassName("updates-text");
    pendingTitle.setClassName("updates-title");
    pendingDescription.setClassName("updates-description");
    textWrap.add(pendingTitle, pendingDescription);

    updateAllButton.addClassName("update-all-btn");
    updateAllButton.addClickListener(e -> updateAll());

    banner.add(iconWrap, textWrap, updateAllButton);
    return banner;
  }

  private Div createFilterTabs() {
    Div tabs = new Div();
    tabs.setClassName("manager-tabs");

    installedTab.setClassName("manager-tab active");
    installedTab.addClickListener(
        e -> {
          showingInstalled = true;
          visibleCards = PAGE_SIZE;
          render();
        });

    availableTab.setClassName("manager-tab");
    availableTab.addClickListener(
        e -> {
          showingInstalled = false;
          visibleCards = PAGE_SIZE;
          render();
        });

    tabs.add(installedTab, availableTab);
    return tabs;
  }

  private Div createLoadMoreSection() {
    Div wrap = new Div();
    wrap.setClassName("load-more-wrap");
    loadMoreButton.setClassName("load-more-btn");
    loadMoreButton.addClickListener(
        e -> {
          visibleCards += PAGE_SIZE;
          render();
        });
    wrap.add(loadMoreButton);
    return wrap;
  }

  private void render() {
    List<Extension> installed = extensions.stream().filter(Extension::isInstalled).toList();
    List<Extension> available = extensions.stream().filter(extension -> !extension.isInstalled()).toList();
    long pending = installed.stream().filter(Extension::isHasUpdate).count();

    installedTab.setText("Installed (" + installed.size() + ")");
    availableTab.setText("Available (" + available.size() + ")");
    installedTab.setClassName("manager-tab" + (showingInstalled ? " active" : ""));
    availableTab.setClassName("manager-tab" + (!showingInstalled ? " active" : ""));

    pendingTitle.setText(pending + " Updates Pending");
    pendingDescription.setText("New versions are available for your installed extensions.");
    updateAllButton.setEnabled(pending > 0);

    cardsGrid.removeAll();
    cardsGrid.setClassName("extension-cards-grid");
    String searchValue = (searchField == null ? "" : searchField.getValue()).toLowerCase();
    List<Extension> source = showingInstalled ? installed : available;
    List<Extension> filtered =
        source.stream()
            .filter(
                extension ->
                    extension.getName() != null
                        && extension.getName().toLowerCase().contains(searchValue))
            .toList();

    filtered.stream()
        .limit(visibleCards)
        .forEach(extension -> cardsGrid.add(createCard(extension)));

    long remaining = Math.max(0, filtered.size() - visibleCards);
    loadMoreButton.setVisible(remaining > 0);
    loadMoreButton.setText("Load More (" + Math.min(PAGE_SIZE, remaining) + " more)");
  }

  private Div createCard(Extension extension) {
    Div card = new Div();
    card.setClassName("extension-card");

    Div header = new Div();
    header.setClassName("card-header");

    Image icon = new Image();
    icon.setClassName("card-icon");
    String baseUrl = settings == null ? "" : settings.getUrl();
    String iconUrl = extension.getIconUrl() == null ? "" : extension.getIconUrl();
    icon.setSrc(baseUrl + iconUrl);
    icon.setAlt(extension.getName());

    Div titleWrap = new Div();
    titleWrap.setClassName("card-title-wrap");
    Span name = new Span(extension.getName());
    name.setClassName("card-name");
    String versionName = extension.getVersionName();
    String versionLabel =
        (versionName == null || versionName.isBlank()) ? "Version unknown" : "v" + versionName;
    String healthLabel =
        extension.isObsolete()
            ? "Action Required"
            : (extension.isHasUpdate() ? "Update Available" : "Up to date");
    Span meta =
        new Span(
            versionLabel + "  •  " + healthLabel);
    meta.setClassName("card-meta");
    titleWrap.add(name, meta);

    Span langBadge = new Span((extension.getLang() == null ? "--" : extension.getLang()).toUpperCase());
    langBadge.setClassName("lang-badge");
    header.add(icon, titleWrap, langBadge);

    Div footer = new Div();
    footer.setClassName("card-footer");
    Button primary = new Button(getPrimaryActionText(extension));
    primary.setClassName("card-primary-btn");
    if (extension.isHasUpdate()) {
      primary.addClassName("is-update");
    }
    primary.addClickListener(e -> runPrimaryAction(extension));

    footer.add(primary);
    card.add(header, footer);
    return card;
  }

  private String getPrimaryActionText(Extension extension) {
    if (extension.isObsolete()) {
      return "Fix Now";
    }
    if (extension.isHasUpdate()) {
      return "Update";
    }
    if (extension.isInstalled()) {
      return "Uninstall";
    }
    return "Install";
  }

  private void runPrimaryAction(Extension extension) {
    boolean success;
    if (extension.isObsolete()) {
      success = extensionService.uninstallExtension(extension.getPkgName());
      if (success) {
        extension.setInstalled(false);
      }
    } else if (extension.isHasUpdate()) {
      success = extensionService.updateExtension(extension.getPkgName());
      if (success) {
        extension.setHasUpdate(false);
      }
    } else if (extension.isInstalled()) {
      success = extensionService.uninstallExtension(extension.getPkgName());
      if (success) {
        extension.setInstalled(false);
      }
    } else {
      success = extensionService.installExtension(extension.getPkgName());
      if (success) {
        extension.setInstalled(true);
      }
    }

    Notification notification =
        Notification.show(success ? "Action completed" : "Action failed", 2500, Notification.Position.BOTTOM_END);
    notification.addThemeVariants(success ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
    reloadExtensions();
  }

  private void updateAll() {
    extensions.stream()
        .filter(extension -> extension.isInstalled() && extension.isHasUpdate())
        .forEach(
            extension -> {
              if (extensionService.updateExtension(extension.getPkgName())) {
                extension.setHasUpdate(false);
              }
            });
    reloadExtensions();
  }

  private void reloadExtensions() {
    try {
      extensions = extensionService.getExtensions();
    } catch (Exception e) {
      log.debug("Couldn't refresh extensions", e);
    }
    render();
  }

  private void redirectToSettings(Exception e) {
    log.debug("Extension data couldn't be created", e);
    UI ui = getUI().orElseGet(UI::getCurrent);
    if (ui == null) {
      log.error("Couldn't access UI", e);
      throw new RuntimeException("Couldn't access UI", e);
    }
    ui.navigate("settings#extensions");
  }
}
