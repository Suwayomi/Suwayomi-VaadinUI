package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Optional;
import online.hatsunemiku.tachideskvaadinui.component.listbox.ChapterListBox;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.MangaDataUtils;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.springframework.web.client.RestTemplate;

@Route("manga/:id(\\d+)")
@CssImport("./css/manga.css")
public class MangaView extends StandardLayout implements BeforeEnterObserver {

  private final RestTemplate client;

  public MangaView(RestTemplate client) {
    super("Manga");
    this.client = client;
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Optional<String> idParam = event.getRouteParameters().get("id");
    Settings settings = SerializationUtils.deseralizeSettings();

    if (idParam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Manga not found");
      return;
    }

    String id = idParam.get();

    Manga manga;
    try {
      manga = getManga(settings, id);
    } catch (Exception e) {
      event.rerouteTo(ServerStartView.class);
      return;
    }

    VerticalLayout container = new VerticalLayout();
    container.addClassName("manga-container");

    Image image = new Image();

    String url = settings.getUrl() + manga.getThumbnailUrl();

    Div imageContainer = new Div();

    image.setSrc(url);
    image.addClassName("manga-image");

    imageContainer.addClassName("manga-image-container");
    imageContainer.add(image);

    container.add(imageContainer);

    ListBox<Chapter> chapters = getChapters(settings, id);

    container.add(chapters);
    setContent(container);
  }


  private Manga getManga(Settings settings, String id) {
    String mangaEndpoint = settings.getUrl() + "/api/v1/manga/" + id;

    Manga manga = client.getForObject(mangaEndpoint, Manga.class);

    if (manga == null) {
      throw new NotFoundException("Manga not found");
    }

    return manga;
  }

  private ListBox<Chapter> getChapters(Settings settings, String mangaId) {

    List<Chapter> chapter = MangaDataUtils.getChapterList(settings, mangaId, client);

    return new ChapterListBox(chapter);
  }
}
