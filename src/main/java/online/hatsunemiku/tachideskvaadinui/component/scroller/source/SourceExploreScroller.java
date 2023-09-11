package online.hatsunemiku.tachideskvaadinui.component.scroller.source;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.DomEvent;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import org.vaadin.firitin.components.orderedlayout.VScroller;

@CssImport("./css/components/scroller/source-explore-scroller.css")
public class SourceExploreScroller extends VScroller {

  private final SourceService sourceService;
  private int currentPage;
  @Getter private final ExploreType type;
  private final long sourceId;
  private final Div content = new Div();
  private final SettingsService settingsService;
  private final ExecutorService pageLoader = Executors.newFixedThreadPool(1);

  public SourceExploreScroller(
      SourceService sourceService,
      ExploreType type,
      long sourceId,
      SettingsService settingsService) {
    super();
    this.sourceService = sourceService;
    this.settingsService = settingsService;
    this.currentPage = 1;
    this.type = type;
    this.sourceId = sourceId;

    setClassName("explore-scroller");

    var reg =
        this.getElement()
            .addEventListener(
                "scroll",
                (DomEvent e) -> {
                  ThreadPoolExecutor executor = (ThreadPoolExecutor) pageLoader;

                  if (executor.getActiveCount() > 0) {
                    return;
                  }

                  double scrollTop = e.getEventData().getNumber("event.target.scrollTop");
                  double scrollHeight = e.getEventData().getNumber("event.target.scrollHeight");
                  double offsetHeight = e.getEventData().getNumber("event.target.offsetHeight");

                  if (scrollHeight == 0 || scrollTop == 0) {
                    return;
                  }

                  double percentage = scrollTop / (scrollHeight - offsetHeight) * 100;

                  if (percentage > 75) {
                    pageLoader.submit(this::loadNextPage);
                  }
                });

    reg.addEventData("event.target.scrollTop");
    reg.addEventData("event.target.scrollHeight");
    reg.addEventData("event.target.offsetHeight");

    content.setClassName("explore-scroller-manga-grid");
    loadNextPage();
    setContent(content);
  }

  private void loadNextPage() {
    var manga =
        switch (type) {
          case POPULAR -> loadPopularPage();
          case LATEST -> loadLatestPage();
        };

    if (manga.isEmpty()) {
      return;
    }

    currentPage++;

    Settings settings = settingsService.getSettings();

    var optUi = getUI();

    UI ui;

    if (optUi.isEmpty()) {
      if (UI.getCurrent() == null) {
        return;
      }

      ui = UI.getCurrent();
    } else {
      ui = optUi.get();
    }

    for (Manga m : manga) {
      ui.access(
          () -> {
            MangaCard card = new MangaCard(settings, m);
            content.add(card);
          });
    }
  }

  private List<Manga> loadPopularPage() {
    var manga = sourceService.getPopularManga(sourceId, currentPage);

    return manga.orElseGet(List::of);
  }

  private List<Manga> loadLatestPage() {
    var manga = sourceService.getLatestManga(sourceId, currentPage);

    return manga.orElseGet(List::of);
  }
}
