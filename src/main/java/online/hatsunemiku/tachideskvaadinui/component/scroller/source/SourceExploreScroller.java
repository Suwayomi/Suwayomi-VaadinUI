package online.hatsunemiku.tachideskvaadinui.component.scroller.source;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.vaadin.firitin.components.orderedlayout.VScroller;

@CssImport("./css/components/scroller/source-explore-scroller.css")
public class SourceExploreScroller extends VScroller {

  private final SourceService sourceService;
  private int currentPage;
  private final ExploreType type;
  private final long sourceId;
  private final Div content = new Div();
  public SourceExploreScroller(SourceService sourceService, ExploreType type, long sourceId) {
    super();
    this.sourceService = sourceService;
    this.currentPage = 1;
    this.type = type;
    this.sourceId = sourceId;

    setClassName("explore-scroller");

    addScrollToEndListener(e -> loadNextPage());

    content.setClassName("explore-scroller-manga-grid");
    loadNextPage();
    setContent(content);
  }

  private void loadNextPage() {
    var manga = switch (type) {
      case POPULAR -> loadPopularPage();
      case LATEST -> loadLatestPage();
    };

    if (manga.isEmpty()) {
      return;
    }

    currentPage++;

    Settings settings = SerializationUtils.deseralizeSettings();

    for (Manga m : manga) {
      content.add(new MangaCard(settings, m));
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

  public ExploreType getType() {
    return type;
  }
}
