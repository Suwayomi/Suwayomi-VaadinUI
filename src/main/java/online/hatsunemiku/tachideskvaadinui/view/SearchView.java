/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Svg;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import feign.FeignException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.component.combo.LangComboBox;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceSearchResult;
import online.hatsunemiku.tachideskvaadinui.services.SearchService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import online.hatsunemiku.tachideskvaadinui.view.trackers.AniListView;
import online.hatsunemiku.tachideskvaadinui.view.trackers.MALView;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.vaadin.miki.shared.text.TextInputMode;
import org.vaadin.miki.superfields.text.SuperTextField;

/**
 * SearchView is a view used for searching sources for manga. It allows the user to search for manga
 * across all sources with a language filter to narrow down the search results.
 */
@CssImport("./css/views/search-view.css")
@Slf4j
@Route("search")
public class SearchView extends StandardLayout implements HasUrlParameter<String> {

  private final Div searchResults;
  private final ComboBox<String> langFilter;
  private final SuperTextField searchField;
  private final SourceService sourceService;
  private final SearchService searchService;
  private final SettingsService settingsService;

  /**
   * Constructs a SearchView object.
   *
   * @param sourceService the {@link SourceService} used for retrieving manga sources
   * @param searchService the {@link SearchService} used for performing search operations
   * @param settingsService the {@link SettingsService} used for accessing search settings
   */
  public SearchView(
      SourceService sourceService, SearchService searchService, SettingsService settingsService) {
    super("Search");

    this.sourceService = sourceService;
    this.searchService = searchService;
    this.settingsService = settingsService;
    searchResults = new Div();

    SuperTextField searchField = createSearchField();
    var langFilter = createLanguageComboBox(sourceService);

    this.searchField = searchField;
    this.langFilter = langFilter;

    Div btnContainer = new Div();
    btnContainer.addClassName("search-btn-container");

    Button aniListImportBtn = getALImportBtn();
    Button malImportBtn = getMalImportBtn();

    btnContainer.add(aniListImportBtn, malImportBtn);

    Div content = new Div();
    content.setClassName("search-content");

    content.add(btnContainer);
    content.add(searchField);
    content.add(langFilter);
    content.add(searchResults);

    setContent(content);
  }

  @NotNull
  private Button getMalImportBtn() {
    Button malImportBtn = new Button("Import from MAL", VaadinIcon.DOWNLOAD.create());

    malImportBtn.addClickListener(
        e -> {
          UI ui = getUI().orElse(UI.getCurrent());

          if (ui == null) {
            return;
          }

          ui.navigate(MALView.class);
        });
    return malImportBtn;
  }

  @NotNull
  private Button getALImportBtn() {
    Button importBtn = new Button("Import from AniList", VaadinIcon.DOWNLOAD.create());
    importBtn.addClickListener(
        e -> {
          UI ui = UI.getCurrent();

          if (ui == null) {
            if (getUI().isEmpty()) {
              log.error("UI is not present");
              return;
            }

            ui = getUI().get();
          }

          ui.navigate(AniListView.class);
        });
    return importBtn;
  }

  @NotNull
  private ComboBox<String> createLanguageComboBox(SourceService sourceService) {
    ComboBox<String> langFilter = new LangComboBox();
    langFilter.addClassName("search-lang-filter");

    var sources = sourceService.getSources();
    var langs = sources.stream().map(Source::getLang).distinct().toList();
    if (!langs.isEmpty()) {
      langFilter.setItems(langs);

      Settings settings = settingsService.getSettings();
      if (settings.hasDefaultSearchLang()) {
        langFilter.setValue(settings.getDefaultSearchLang());
      }
    }

    langFilter.addValueChangeListener(
        e -> {
          if (!e.isFromClient()) {
            return;
          }
          runSearch(searchField);
        });

    return langFilter;
  }

  @NotNull
  private SuperTextField createSearchField() {
    SuperTextField searchField = new SuperTextField("Search");
    searchField.setTextInputMode(TextInputMode.SEARCH);
    searchField.setClearButtonVisible(true);
    searchField.setAutoselect(true);
    searchField.addClassName("search-field");
    searchField.addValueChangeListener(e -> runSearch(searchField));
    searchField.addKeyDownListener(Key.ENTER, e -> runSearch(searchField));

    return searchField;
  }

  private void runSearch(SuperTextField searchField) {
    if (searchField.isEmpty()) {
      searchResults.removeAll();
      return;
    }

    searchField.setSuffixComponent(getLoadingDiv());
    searchField.setReadOnly(true);
    langFilter.setReadOnly(true);
    searchResults.removeAll();

    CompletableFuture<?> future = CompletableFuture.runAsync(() -> search(searchField.getValue()));

    future
        .thenRun(
            () -> {
              var ui = getUI();

              if (ui.isEmpty()) {
                log.error("UI is not present");
                return;
              }

              if (!ui.get().isAttached()) {
                log.debug("UI is not attached anymore");
                return;
              }

              ui.get()
                  .access(
                      () -> {
                        searchField.setSuffixComponent(null);
                        searchField.setReadOnly(false);
                        langFilter.setReadOnly(false);
                      });
            })
        .exceptionally(
            ex -> {
              log.error("Error searching", ex);
              return null;
            });
  }

  private Div getLoadingDiv() {
    Div loadingDiv = new Div();
    loadingDiv.setClassName("loading-div");

    Resource loadingSvgResource = new ClassPathResource("images/loading.svg");
    try {
      Svg loadingSvg = new Svg(loadingSvgResource.getInputStream());
      loadingDiv.add(loadingSvg);
    } catch (IOException e) {
      log.error("Error loading loading.svg", e);
    }

    return loadingDiv;
  }

  public void search(String query) {
    var sources = sourceService.getSources();
    var langGroupedSources =
        sources.stream()
            .sorted((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()))
            .collect(Collectors.groupingBy(Source::getLang));

    searchSources(query, langGroupedSources);
  }

  /**
   * Adds a search result to the user interface.
   *
   * @param source the source of the search result
   * @param mangaList the list of manga from the search result
   * @return true if the search result was successfully added, otherwise false
   */
  private boolean addSearchResultToUI(Source source, List<Manga> mangaList) {
    Div searchResult = createSearchResultDiv(source, mangaList);
    UI realUI;

    var ui = getUI();

    if (ui.isEmpty()) {
      if (UI.getCurrent() == null) {
        log.error("UI is not present");
        return false;
      }

      realUI = UI.getCurrent();
    } else {
      realUI = ui.get();
    }

    if (!realUI.isAttached()) {
      log.debug("UI is not attached anymore");
      return false;
    }

    realUI.access(() -> searchResults.add(searchResult));
    return true;
  }

  private Div createSearchResultDiv(Source source, List<Manga> mangaList) {
    Div searchResult = new Div();
    searchResult.setClassName("search-result");

    Settings settings = settingsService.getSettings();

    Div titleContainer = new Div();
    Image sourceIcon = new Image(settings.getUrl() + source.getIconUrl(), "Icon");
    sourceIcon.setClassName("search-result-icon");
    titleContainer.add(sourceIcon);

    Div title = new Div();

    title.setText(source.getDisplayName());
    titleContainer.add(title);
    titleContainer.setClassName("search-result-title");

    Div mangaListDiv = new Div();
    mangaListDiv.setClassName("search-result-manga-list");

    for (var manga : mangaList) {
      MangaCard mangaCard = new MangaCard(settings, manga);
      mangaListDiv.add(mangaCard);
    }

    searchResult.add(titleContainer);
    searchResult.add(mangaListDiv);

    return searchResult;
  }

  private void searchSources(String query, Map<String, List<Source>> langGroupedSources) {
    for (var langSet : langGroupedSources.entrySet()) {

      var lang = langSet.getKey();

      if (!langFilter.isEmpty() && !langFilter.getValue().equals(lang)) {
        continue;
      }

      var langSources = langGroupedSources.get(lang);

      List<Callable<Void>> searchTasks = new ArrayList<>();
      for (var source : langSources) {
        Callable<Void> runnable =
            () -> {
              searchSource(query, source);
              return null;
            };
        searchTasks.add(runnable);
      }

      // When upgrading to Java 21 put the executor in a try-with-resources block instead
      var executor = Executors.newCachedThreadPool();
      try {
        executor.invokeAll(searchTasks);
        executor.shutdown();
      } catch (InterruptedException e) {
        log.error("Error waiting for search to finish", e);
      }
    }
  }

  private void searchSource(String query, Source source) {
    log.info("Started searching source {}", source.getDisplayName());
    boolean hasNext = true;

    List<Manga> mangaList = new ArrayList<>();

    // skipqc: JAVA-E0214
    for (int i = 1; hasNext; i++) {
      SourceSearchResult searchResponse;
      try {
        searchResponse = searchService.search(query, source.getId(), i);
      } catch (FeignException e) {
        String message = "Source %s failed with error code %d";
        String errorMessage = String.format(message, source.getDisplayName(), e.status());

        Notification notification = new Notification(errorMessage, 3000, Position.BOTTOM_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        log.error(errorMessage);

        UI ui = UI.getCurrent();

        if (ui == null) {
          if (getUI().isEmpty()) {
            log.error("UI is not present");
            return;
          }

          ui = getUI().get();
        }

        ui.access(notification::open);
        return;
      }

      if (searchResponse.manga().isEmpty()) {
        break;
      }

      mangaList.addAll(searchResponse.manga());

      hasNext = searchResponse.hasNextPage();
    }

    if (mangaList.isEmpty()) {
      return;
    }

    if (!addSearchResultToUI(source, mangaList)) {
      log.error("Failed to add search result to UI for source {}", source.getDisplayName());
    }
  }

  @Override
  public void setParameter(BeforeEvent event, @OptionalParameter String query) {
    if (query == null) {
      return;
    }

    searchField.setValue(query);
    runSearch(searchField);
  }
}
