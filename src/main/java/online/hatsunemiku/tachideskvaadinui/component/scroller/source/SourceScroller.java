package online.hatsunemiku.tachideskvaadinui.component.scroller.source;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceFilterUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceLangFilterUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceLangUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.items.BlurryItem;
import online.hatsunemiku.tachideskvaadinui.component.items.LangItem;
import online.hatsunemiku.tachideskvaadinui.component.items.SourceItem;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.ServerStartView;
import org.vaadin.firitin.components.orderedlayout.VScroller;

@CssImport("./css/components/source-scroller.css")
public class SourceScroller extends VScroller {

  private final SourceService service;
  private final List<List<Source>> filteredSources;
  private final SettingsService settingsService;
  private List<String> languages;
  private final Div content;
  private int currentIndex = 0;
  private int languageIndex = 0;
  private static final int LIST_SIZE = 15;
  private boolean isDone = false;
  private String filterLanguage = "";
  private String filterText = "";

  public SourceScroller(SourceService service, SettingsService settingsService) {
    super();
    setClassName("source-scroller");

    this.service = service;
    this.settingsService = settingsService;

    List<Source> sourceList;
    try {
      sourceList = service.getSources();
    } catch (Exception e) {
      getUI().ifPresent(ui -> ui.access(() -> ui.navigate(ServerStartView.class)));
      this.filteredSources = new ArrayList<>();
      this.content = new Div();
      setLanguages(List.of());
      return;
    }
    List<Source> sources = new ArrayList<>(sourceList);

    setLanguages(getLanguages(sources));

    this.content = new Div();
    content.setClassName("source-scroller-content");
    this.filteredSources = new ArrayList<>();

    sort(sources);

    for (String language : languages) {
      List<Source> filtered = filterLang(language, sources);

      if (filtered.isEmpty()) {
        continue;
      }

      filteredSources.add(filtered);
    }

    Settings settings = settingsService.getSettings();

    addNextContent(settings);
    setContent(content);

    addScrollToEndListener(e -> addNextContent(settings));

    ComponentUtil.addListener(
        UI.getCurrent(), SourceFilterUpdateEvent.class, this::onComponentEvent);
    ComponentUtil.addListener(
        UI.getCurrent(), SourceLangFilterUpdateEvent.class, this::onComponentEvent);
  }

  private void sort(List<Source> sources) {
    sources.sort(Comparator.comparing(Source::getName));
  }

  private Comparator<String> getLangComp() {
    return (o1, o2) -> {
      if (o1.equals("localsourcelang")) {
        return -1;
      }
      if (o2.equals("localsourcelang")) {
        return 1;
      }
      return o1.compareTo(o2);
    };
  }

  private void addNextContent(Settings settings) {
    List<BlurryItem> items = new ArrayList<>();
    getNextContent(settings, items);
    for (BlurryItem source : items) {
      content.add(source);
    }
  }

  private void getNextContent(Settings settings, List<BlurryItem> items) {

    if (isDone) {
      return;
    }

    if (languageIndex >= filteredSources.size()) {
      return;
    }

    if (items.size() >= LIST_SIZE) {
      return;
    }

    List<Source> sources = filteredSources.get(languageIndex);

    if (currentIndex >= sources.size()) {
      switchToNextLang(items, settings);
      if (isDone) {
        return;
      }
    }

    int endIndex = currentIndex + LIST_SIZE;

    if (endIndex > sources.size()) {
      endIndex = sources.size();
    }

    List<Source> subList = new ArrayList<>(sources.subList(currentIndex, endIndex));

    for (Source source : subList) {
      SourceItem item = new SourceItem(source, settings);
      items.add(item);
    }

    if (subList.size() < LIST_SIZE) {
      switchToNextLang(items, settings);
    } else {
      currentIndex = endIndex;
    }
  }

  private void switchToNextLang(List<BlurryItem> subList, Settings settings) {
    currentIndex = 0;
    languageIndex++;

    if (languageIndex >= languages.size()) {
      isDone = true;
      return;
    }

    if (subList.size() >= LIST_SIZE) {
      return;
    }

    String lang = languages.get(languageIndex);
    LangItem langItem = new LangItem(lang);
    subList.add(langItem);

    getNextContent(settings, subList);
  }

  private List<String> getLanguages(List<Source> sources) {

    if (filterLanguage != null && !filterLanguage.isBlank()) {
      return List.of(filterLanguage);
    }

    return sources.parallelStream().map(Source::getLang).distinct().sorted(getLangComp()).toList();
  }

  private List<Source> filterLang(String lang, List<Source> sources) {
    return sources.parallelStream().filter(source -> source.getLang().equals(lang)).toList();
  }

  private List<Source> filterSources(String search, List<Source> sources) {
    if (search == null || search.isBlank()) {
      return sources;
    }

    return sources.parallelStream()
        .filter(source -> source.getName().toLowerCase().contains(search.toLowerCase()))
        .toList();
  }

  private void setLanguages(List<String> languages) {
    this.languages = new ArrayList<>(languages);
    SourceLangUpdateEvent event = new SourceLangUpdateEvent(this, languages);
    fireEvent(event);
  }

  public void addLangUpdateEventListener(ComponentEventListener<SourceLangUpdateEvent> listener) {
    addListener(SourceLangUpdateEvent.class, listener);

    // makes sure the new listener gets the current languages
    var event = new SourceLangUpdateEvent(this, languages);
    fireEvent(event);
  }

  public void onComponentEvent(SourceFilterUpdateEvent event) {
    content.removeAll();
    currentIndex = 0;
    languageIndex = 0;
    isDone = false;

    List<Source> sources = new ArrayList<>(service.getSources());

    sort(sources);

    this.filterText = event.getFilterText();

    if (!event.getFilterText().isBlank()) {
      sources = filterSources(event.getFilterText(), sources);
    }

    updateSources(sources);
  }

  public void onComponentEvent(SourceLangFilterUpdateEvent event) {
    content.removeAll();
    currentIndex = 0;
    languageIndex = 0;
    isDone = false;

    List<Source> sources = new ArrayList<>(service.getSources());

    sort(sources);

    if (event.getFilterLanguage() == null) {
      return;
    }

    this.filterLanguage = event.getFilterLanguage();

    updateSources(sources);

    Settings settings = settingsService.getSettings();

    addNextContent(settings);
  }

  private void updateSources(List<Source> sources) {

    if (!filterText.isBlank()) {
      sources = filterSources(filterText, sources);
    }

    this.languages = getLanguages(sources);
    filteredSources.clear();

    for (String language : languages) {
      if (!filterLanguage.isBlank() && !language.equals(filterLanguage)) {
        continue;
      }

      List<Source> filtered = filterLang(language, sources);

      if (filtered.isEmpty()) {
        continue;
      }

      filteredSources.add(filtered);
    }

    Settings settings = settingsService.getSettings();

    addNextContent(settings);
  }
}
