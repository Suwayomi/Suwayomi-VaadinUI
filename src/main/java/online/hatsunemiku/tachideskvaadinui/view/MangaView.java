package online.hatsunemiku.tachideskvaadinui.view;

import static org.springframework.http.HttpMethod.GET;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import online.hatsunemiku.tachideskvaadinui.data.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.Manga;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

@Route("manga/:id(\\d+)")
@CssImport("./css/manga.css")
public class MangaView extends StandardLayout implements BeforeEnterObserver {

  private static final Logger logger = LoggerFactory.getLogger(MangaView.class);
  private RestTemplate client;

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

    Manga manga = getManga(settings, id);

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

    List<HorizontalLayout> chapters = getChapters(settings, id);

    for (HorizontalLayout c : chapters) {
      container.add(c);
    }

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

  private List<HorizontalLayout> getChapters(Settings settings, String mangaId) {
    String chapterEndpoint = settings.getUrl() + "/api/v1/manga/" + mangaId + "/chapters";

    var typeRef = new ParameterizedTypeReference<List<Chapter>>() {};
    var chapter = client.exchange(chapterEndpoint, GET, null, typeRef)
        .getBody();

    if (chapter == null) {
      return new ArrayList<>();
    }

    List<HorizontalLayout> chapters = new ArrayList<>();

    for (Chapter c : chapter) {
      HorizontalLayout layout = new HorizontalLayout();
      layout.addClassName("chapter");

      String title = "Chapter " + c.getChapterNumber();

      Date date = new Date(c.getUploadDate());

      SimpleDateFormat formatter = new SimpleDateFormat("dd/MMMM/yyyy");

      String uploadDate = "Released on " + formatter.format(date);


      Div titleDiv = new Div();
      titleDiv.addClassName("chapter-title");
      titleDiv.setText(title);

      Div uploadDateDiv = new Div();
      uploadDateDiv.addClassName("chapter-upload-date");
      uploadDateDiv.setText(uploadDate);

      layout.add(titleDiv, uploadDateDiv);

      chapters.add(layout);
    }

    return chapters;
  }
}
