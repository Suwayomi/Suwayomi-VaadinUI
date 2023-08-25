package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingService;
import online.hatsunemiku.tachideskvaadinui.utils.NavigationUtils;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;
import online.hatsunemiku.tachideskvaadinui.view.RootView;
import org.jetbrains.annotations.NotNull;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.Swiper;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.SwiperConfig;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.constants.LanguageDirection;

@CssImport("./css/components/reader/manga-reader.css")
@Slf4j
public class MangaReader extends Div {

  public MangaReader(
      Chapter chapter,
      SettingsService settingsService,
      MangaService mangaService,
      TrackingService trackingService,
      boolean hasNext) {
    addClassName("manga-reader");

    Reader reader = new Reader(chapter, settingsService, trackingService, mangaService);
    Sidebar sidebar = new Sidebar(mangaService, chapter, reader.swiper, hasNext);
    Controls controls = new Controls(reader, hasNext, chapter);
    add(sidebar, reader, controls);
  }

  private static class Sidebar extends Div {

    public Sidebar(MangaService mangaService, Chapter chapter, Swiper swiper, boolean hasNext) {
      addClassName("sidebar");

      Button home = getHomeButton();

      List<Chapter> chapters = mangaService.getChapterList(chapter.getMangaId());
      Div chapterSelect = new Div();
      chapterSelect.setClassName("chapter-select");
      chapterSelect.getStyle().set("--vaadin-combo-box-overlay-width", "20vw");

      Button leftBtn = getChapterLeftBtn(swiper, chapter, hasNext);

      ComboBox<Chapter> chapterComboBox = getChapterComboBox(chapter, chapters);

      Button rightBtn = getChapterRightBtn(swiper, chapter, hasNext);

      chapterSelect.add(leftBtn, chapterComboBox, rightBtn);

      add(home, chapterSelect);
    }

    @NotNull
    private Button getChapterRightBtn(Swiper swiper, Chapter chapter, boolean hasNext) {
      Button rightBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
      rightBtn.setId("rightBtn");
      rightBtn.addClickListener(
          e -> {
            int chapterIndex = chapter.getIndex();

            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {

              if (chapterIndex <= 1) {
                return;
              }

              chapterIndex--;
            } else {

              if (!hasNext) {
                return;
              }

              chapterIndex++;
            }

            UI ui = UI.getCurrent();

            NavigationUtils.navigateToReader(chapter.getMangaId(), chapterIndex, ui);
          });
      return rightBtn;
    }

    @NotNull
    private ComboBox<Chapter> getChapterComboBox(Chapter chapter, List<Chapter> chapters) {
      ComboBox<Chapter> chapterComboBox = new ComboBox<>();
      chapterComboBox.setRenderer(createRenderer());
      chapterComboBox.setItems(chapters);
      chapterComboBox.setValue(chapter);
      chapterComboBox.setAllowCustomValue(false);
      chapterComboBox.addValueChangeListener(
          e -> {
            if (!e.isFromClient()) {
              return;
            }

            Chapter c = e.getValue();

            if (c == null) {
              return;
            }

            UI ui = getUI().orElseThrow();

            String mangaId = String.valueOf(c.getMangaId());
            String chapterIndex = String.valueOf(c.getIndex());

            RouteParam mangaIdParam = new RouteParam("mangaId", mangaId);
            RouteParam chapterIndexParam = new RouteParam("chapterIndex", chapterIndex);

            RouteParameters params = new RouteParameters(mangaIdParam, chapterIndexParam);
            ui.navigate(ReadingView.class, params);
          });
      return chapterComboBox;
    }

    @NotNull
    private Button getChapterLeftBtn(Swiper swiper, Chapter chapter, boolean hasNext) {
      Button leftBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
      leftBtn.setId("leftBtn");
      leftBtn.addClickListener(
          e -> {
            int chapterIndex = chapter.getIndex();

            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {

              if (!hasNext) {
                return;
              }

              chapterIndex++;
            } else {

              if (chapterIndex <= 1) {
                return;
              }

              chapterIndex--;
            }

            UI ui = UI.getCurrent();

            NavigationUtils.navigateToReader(chapter.getMangaId(), chapterIndex, ui);
          });
      return leftBtn;
    }

    private Renderer<Chapter> createRenderer() {
      String template = """
          <div>${item.name}</div>
          """;

      return LitRenderer.<Chapter>of(template).withProperty("name", Chapter::getName);
    }

    @NotNull
    private static Button getHomeButton() {
      Button home = new Button(VaadinIcon.HOME.create());
      home.setId("homeBtn");
      home.addClickListener(e -> UI.getCurrent().navigate(RootView.class));
      return home;
    }
  }

  private static class Reader extends Div {

    private final Chapter chapter;
    private final Swiper swiper;
    private final SettingsService settingsService;

    public Reader(
        Chapter chapter,
        SettingsService settingsService,
        TrackingService trackingService,
        MangaService mangaService) {
      addClassName("reader");
      this.chapter = chapter;
      this.settingsService = settingsService;

      var config = SwiperConfig.builder().centeredSlides(true).build();

      swiper = new Swiper(config);
      swiper.changeLanguageDirection(LanguageDirection.RIGHT_TO_LEFT);

      loadChapter();

      Tracker tracker = settingsService.getSettings().getTracker(chapter.getMangaId());

      if (tracker.hasAniListId()) {
        swiper.addActiveIndexChangeEventListener(
            e -> {
              if (e.getActiveIndex() == chapter.getPageCount() - 1) {
                log.info("Last page of chapter {}", chapter.getIndex());
                trackingService.setChapterProgress(chapter.getMangaId(), chapter.getIndex(), true);
                e.unregisterListener();
              }
            });
      }

      swiper.addReachEndEventListener(
          e -> {
            int mangaId = chapter.getMangaId();
            int chapterIndex = chapter.getIndex();
            if (mangaService.setChapterRead(mangaId, chapterIndex)) {
              log.info("Set chapter {} to read", chapter.getName());
            } else {
              log.warn("Couldn't set chapter {} to read", chapter.getName());
            }
          });

      add(swiper);
    }

    private void loadChapter() {
      Settings settings = settingsService.getSettings();
      String baseUrl = settings.getUrl();
      int mangaId = chapter.getMangaId();
      int chapterIndex = chapter.getIndex();
      String format = "%s/api/v1/manga/%d/chapter/%d/page/%d";

      for (int i = 0; i < chapter.getPageCount(); i++) {
        String url = String.format(format, baseUrl, mangaId, chapterIndex, i);

        Image image = new Image(url, "Page %d".formatted(i + 1));

        if (i > 1) {
          image.getElement().setAttribute("loading", "lazy");
        }

        image.addClassName("manga-page");

        swiper.add(image);
      }
    }
  }

  private static class Controls extends Div {

    private final int pageCount;
    private final int mangaId;
    private final int chapterIndex;
    private final boolean hasNext;

    public Controls(Reader reader, boolean hasNext, Chapter chapter) {
      addClassName("controls");

      this.pageCount = chapter.getPageCount();
      this.mangaId = chapter.getMangaId();
      this.chapterIndex = chapter.getIndex();
      this.hasNext = hasNext;

      Button left = getPrevButton(reader);

      Div pageTrack = new Div();

      TextField input = new TextField("", "1", "");
      input.setAllowedCharPattern("\\d");
      input.addValueChangeListener(
          e -> {
            if (e.getValue().isEmpty()) {
              log.debug("Value is empty");
              input.setValue(e.getOldValue());
              return;
            }

            if (!e.getValue().matches("\\d+")) {
              log.debug("Value is not a number");
              input.setValue(e.getOldValue());
              return;
            }

            int value = Integer.parseInt(e.getValue());

            if (value == reader.swiper.getActiveIndex()) {
              log.debug("Value is the same as active index");
              input.setValue(e.getOldValue());
              return;
            }

            if (value > pageCount || value < 1) {
              log.debug("Value is out of bounds");
              input.setValue(e.getOldValue());
              return;
            }

            reader.swiper.slideTo(value - 1);
            log.debug("Value changed to {}", value);
          });

      Text totalChapters = new Text("/ " + pageCount);

      reader.swiper.addActiveIndexChangeEventListener(
          e -> {
            int activeIndex = e.getActiveIndex() + 1;
            input.setValue(String.valueOf(activeIndex));
          });

      pageTrack.add(input, totalChapters);

      Button right = getNextButton(reader);

      add(left, pageTrack, right);
    }

    @NotNull
    private Button getNextButton(Reader reader) {
      Icon arrowRight = VaadinIcon.ARROW_RIGHT.create();
      Button right = new Button(arrowRight);
      right.addClickListener(
          e -> {
            Swiper swiper = reader.swiper;
            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
              prevPage(swiper);
            } else {
              nextPage(swiper);
            }
          });

      if (reader.swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
        right.addClickShortcut(Key.ARROW_RIGHT);
      } else {
        right.addClickShortcut(Key.ARROW_LEFT);
      }

      right.setIconAfterText(true);
      return right;
    }

    @NotNull
    private Button getPrevButton(Reader reader) {
      Icon arrowLeft = VaadinIcon.ARROW_LEFT.create();
      Button left = new Button(arrowLeft);
      left.addClickListener(
          e -> {
            Swiper swiper = reader.swiper;
            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
              nextPage(swiper);
            } else {
              prevPage(swiper);
            }
          });

      if (reader.swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
        left.addClickShortcut(Key.ARROW_LEFT);
      } else {
        left.addClickShortcut(Key.ARROW_RIGHT);
      }
      return left;
    }

    private void nextPage(Swiper swiper) {

      if (swiper.getActiveIndex() != pageCount - 1) {
        swiper.slideNext();
        return;
      }

      if (!hasNext) {
        return;
      }

      UI ui = getUI().orElseThrow();

      RouteParam mangaIdParam = new RouteParam("mangaId", String.valueOf(this.mangaId));
      int newChapterIndex = this.chapterIndex + 1;
      RouteParam chapterIndexParam =
          new RouteParam("chapterIndex", String.valueOf(newChapterIndex));

      RouteParameters params = new RouteParameters(mangaIdParam, chapterIndexParam);

      ui.navigate(ReadingView.class, params);
    }

    private void prevPage(Swiper swiper) {

      if (swiper.getActiveIndex() != 0) {
        swiper.slidePrev();
        return;
      }

      if (chapterIndex <= 1) {
        return;
      }

      UI ui = getUI().orElseThrow();

      RouteParam mangaIdParam = new RouteParam("mangaId", String.valueOf(this.mangaId));
      int newChapterIndex = this.chapterIndex - 1;
      RouteParam chapterIndexParam =
          new RouteParam("chapterIndex", String.valueOf(newChapterIndex));

      RouteParameters params = new RouteParameters(mangaIdParam, chapterIndexParam);

      ui.navigate(ReadingView.class, params);
    }
  }
}
