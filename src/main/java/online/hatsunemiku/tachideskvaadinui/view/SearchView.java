package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Svg;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.Route;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.component.combo.LangComboBox;
import online.hatsunemiku.tachideskvaadinui.component.events.source.LanguageListChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.services.SearchService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.vaadin.miki.shared.text.TextInputMode;
import org.vaadin.miki.superfields.text.SuperTextField;

@CssImport("./css/views/search-view.css")
@Slf4j
@Route("search")
public class SearchView extends StandardLayout {

  private final Div searchResults;
  private final LangComboBox langFilter;
  private final SuperTextField searchField;
  private final SourceService sourceService;
  private final SearchService searchService;
  private final SettingsService settingsService;

  public SearchView(
      SourceService sourceService, SearchService searchService, SettingsService settingsService) {
    super("Search");

    this.sourceService = sourceService;
    this.searchService = searchService;
    this.settingsService = settingsService;
    searchResults = new Div();

    SuperTextField searchField = createSearchField();
    LangComboBox langFilter = createLanguageComboBox(sourceService);

    this.searchField = searchField;
    this.langFilter = langFilter;

    Div content = new Div();
    content.setClassName("search-content");

    content.add(searchField);
    content.add(langFilter);
    content.add(searchResults);

    setContent(content);
  }

  @NotNull
  private LangComboBox createLanguageComboBox(SourceService sourceService) {
    LangComboBox langFilter = new LangComboBox();
    langFilter.addClassName("search-lang-filter");

    addListener(LanguageListChangeEvent.class, langFilter);
    CompletableFuture.runAsync(
        () -> {
          var sources = sourceService.getSources();
          var langs = sources.stream().map(Source::getLang).distinct().toList();
          LanguageListChangeEvent event = new LanguageListChangeEvent(this, langs);
          fireEvent(event);
        });

    langFilter.addValueChangeListener(e -> runSearch(searchField));

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
    var langGroupedSources = sources.stream().collect(Collectors.groupingBy(Source::getLang));

    var filteredMap = new TreeMap<>(searchSources(query, langGroupedSources));

    for (var entry : filteredMap.entrySet()) {
      Div searchResult = createSearchResultDiv(entry.getKey(), entry.getValue());
      var ui = getUI();

      if (ui.isEmpty()) {
        log.error("UI is not present");
        return;
      }

      if (!ui.get().isAttached()) {
        log.debug("UI is not attached anymore");
        return;
      }

      ui.get().access(() -> searchResults.add(searchResult));
    }
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

  @NotNull
  private Map<Source, List<Manga>> searchSources(
      String query, Map<String, List<Source>> langGroupedSources) {
    HashMap<Source, List<Manga>> mangaMap = new HashMap<>();

    for (var lang : langGroupedSources.keySet()) {

      if (!langFilter.isEmpty() && !langFilter.getValue().equals(lang)) {
        continue;
      }

      var langSources = langGroupedSources.get(lang);

      for (var source : langSources) {
        searchSource(query, source, mangaMap);
      }
    }

    return mangaMap.entrySet().stream()
        .filter(entry -> !entry.getValue().isEmpty())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private void searchSource(String query, Source source, HashMap<Source, List<Manga>> mangaMap) {
    boolean hasNext = true;
    for (int i = 1; hasNext; i++) {
      hasNext = searchPage(query, source, mangaMap, i);
    }
  }

  private boolean searchPage(
      String query, Source source, HashMap<Source, List<Manga>> mangaMap, int pageNum) {
    var searchResponse = searchService.search(query, source.getId(), pageNum);

    if (searchResponse.mangaList().isEmpty()) {
      return false;
    }

    mangaMap.putIfAbsent(source, new ArrayList<>());

    mangaMap.get(source).addAll(searchResponse.mangaList());

    return searchResponse.hasNext();
  }
}
