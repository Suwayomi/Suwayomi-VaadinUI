/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
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
import com.vaadin.flow.shared.Registration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.ReaderSettingsChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingCommunicationService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.utils.NavigationUtils;
import online.hatsunemiku.tachideskvaadinui.view.RootView;
import org.jetbrains.annotations.NotNull;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.Swiper;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.SwiperConfig;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.constants.LanguageDirection;

@CssImport("./css/components/reader/manga-reader.css")
@Slf4j
public class MangaReader extends Div {

  private final SettingsService settingsService;
  private final MangaService mangaService;
  private final ExecutorService trackerExecutor;
  private final int chapterIndex;
  private final List<Chapter> chapters;

  public MangaReader(
      Chapter chapter,
      SettingsService settingsService,
      TrackingDataService dataService,
      MangaService mangaService,
      TrackingCommunicationService trackingCommunicationService,
      List<Chapter> chapters) {
    addClassName("manga-reader");

    this.settingsService = settingsService;
    this.mangaService = mangaService;
    this.trackerExecutor = Executors.newSingleThreadExecutor();
    this.chapterIndex = chapters.stream().map(Chapter::getId).toList().indexOf(chapter.getId());
    this.chapters = List.copyOf(chapters);

    Reader reader = new Reader(chapter, dataService, trackingCommunicationService, mangaService);
    Sidebar sidebar = new Sidebar(mangaService, chapter, reader.swiper);
    Controls controls = new Controls(reader, chapter, chapterIndex);
    add(sidebar, reader, controls);
  }

  public Registration addReaderChapterChangeEventListener(
      ComponentEventListener<ReaderChapterChangeEvent> listener) {
    return addListener(ReaderChapterChangeEvent.class, listener);
  }

  // skipcq: JAVA-W1019
  private class Sidebar extends Div {

    public Sidebar(MangaService mangaService, Chapter chapter, Swiper swiper) {
      addClassName("sidebar");

      Div navigationButtons = getNavigationButtons(chapter);

      List<Chapter> chapters = mangaService.getChapterList(chapter.getMangaId());

      if (chapters.isEmpty()) {
        chapters = mangaService.fetchChapterList(chapter.getMangaId());
      }

      Div chapterSelect = new Div();
      chapterSelect.setClassName("chapter-select");
      chapterSelect.getStyle().set("--vaadin-combo-box-overlay-width", "20vw");

      Button leftBtn = getChapterLeftBtn(swiper, chapter);

      ComboBox<Chapter> chapterComboBox = getChapterComboBox(chapter, chapters);

      Button rightBtn = getChapterRightBtn(swiper, chapter);

      chapterSelect.add(leftBtn, chapterComboBox, rightBtn);

      Button settingsBtn = new Button(VaadinIcon.COG.create());
      settingsBtn.setId("settings-btn");
      settingsBtn.addClickListener(
          e -> {
            var dialog =
                new ReaderSettingsDialog(settingsService.getSettings(), chapter.getMangaId());
            dialog.open();
          });

      add(navigationButtons, chapterSelect, settingsBtn);
    }

    @NotNull
    private Div getNavigationButtons(Chapter chapter) {
      Div navigationButtons = new Div();
      navigationButtons.addClassName("navigation-buttons");

      Button home = getHomeButton();
      Button backToManga = getBackToMangaButton(chapter);

      navigationButtons.add(home, backToManga);
      return navigationButtons;
    }

    @NotNull
    private Button getBackToMangaButton(Chapter chapter) {
      Button backToManga = new Button(VaadinIcon.BOOK.create());

      int mangaId = chapter.getMangaId();
      var ui = getUI().orElseGet(UI::getCurrent);

      if (ui == null) {
        log.error("UI could not be accessed.");
        throw new IllegalStateException("UI could not be accessed.");
      }

      backToManga.addClickListener(e -> NavigationUtils.navigateToManga(mangaId, ui));
      return backToManga;
    }

    @NotNull
    private Button getChapterRightBtn(Swiper swiper, Chapter chapter) {
      Button rightBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
      rightBtn.setId("rightBtn");
      rightBtn.addClickListener(
          e -> {
            int newChapterId;

            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {

              if (chapterIndex == 0) {
                return;
              }

              newChapterId = chapters.get(chapterIndex - 1).getId();
            } else {

              if (chapterIndex == chapters.size() - 1) {
                return;
              }

              newChapterId = chapters.get(chapterIndex + 1).getId();
            }

            int mangaId = chapter.getMangaId();
            var changeEvent =
                new ReaderChapterChangeEvent(
                    MangaReader.this, false, mangaId, newChapterId, chapters);
            MangaReader.this.fireEvent(changeEvent);
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

            if (Objects.equals(e.getOldValue(), e.getValue())) {
              return;
            }

            Chapter c = e.getValue();

            if (c == null) {
              return;
            }

            var mangaId = c.getMangaId();
            var chapterId = c.getId();

            var event =
                new ReaderChapterChangeEvent(MangaReader.this, false, mangaId, chapterId, chapters);

            MangaReader.this.fireEvent(event);
          });
      return chapterComboBox;
    }

    @NotNull
    private Button getChapterLeftBtn(Swiper swiper, Chapter chapter) {
      Button leftBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
      leftBtn.setId("leftBtn");
      leftBtn.addClickListener(
          e -> {
            int newChapterId;

            int mangaId = chapter.getMangaId();
            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {

              if (chapterIndex >= chapters.size() - 1) {
                return;
              }

              newChapterId = chapters.get(chapterIndex + 1).getId();
            } else {

              if (chapterIndex == 0) {
                return;
              }
              newChapterId = chapters.get(chapterIndex - 1).getId();
            }

            var changeEvent =
                new ReaderChapterChangeEvent(
                    MangaReader.this, false, mangaId, newChapterId, chapters);

            MangaReader.this.fireEvent(changeEvent);
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

  private class Reader extends Div {

    private final Chapter chapter;
    private final Swiper swiper;
    private final MangaService mangaService;

    public Reader(
        Chapter chapter,
        TrackingDataService dataService,
        TrackingCommunicationService trackingCommunicationService,
        MangaService mangaService) {
      addClassName("reader");
      this.chapter = chapter;
      this.mangaService = mangaService;

      var config = SwiperConfig.builder().zoom(true).centeredSlides(true).build();

      swiper = new Swiper(config);

      UI ui = UI.getCurrent();
      ComponentUtil.addListener(
          ui,
          ReaderSettingsChangeEvent.class,
          e -> {
            var direction = e.getNewSettings().getDirection();

            switch (direction) {
              case RTL -> swiper.changeLanguageDirection(LanguageDirection.RIGHT_TO_LEFT);
              case LTR -> swiper.changeLanguageDirection(LanguageDirection.LEFT_TO_RIGHT);
              default -> throw new IllegalStateException("Unexpected value: " + direction);
            }
          });

      ReaderSettings settings =
          settingsService.getSettings().getReaderSettings(chapter.getMangaId());

      switch (settings.getDirection()) {
        case RTL -> swiper.changeLanguageDirection(LanguageDirection.RIGHT_TO_LEFT);
        case LTR -> swiper.changeLanguageDirection(LanguageDirection.LEFT_TO_RIGHT);
        default -> throw new IllegalStateException("Unexpected value: " + settings.getDirection());
      }

      /*This is a JavaScript function as it feels more sluggish when it has
       * to send data back to the server. Therefore, the server is responsible
       * for the mouse wheel's zoom function.
       */
      swiper
          .getElement()
          .executeJs(
              """
                      var zoomListener = function (e) {
                       if ($0.swiper.zoom === undefined) {
                        console.info("Removing zoom listener.");
                        removeEventListener('wheel', zoomListener);
                        return;
                       }

                        var zoom = $0.swiper.zoom.scale;
                        if (e.deltaY < 0) {
                          zoom += 0.5;
                        } else {
                          zoom -= 0.5;
                        }

                        if (zoom < 1) {
                          zoom = 1;
                        }

                        if (zoom > 3) {
                          zoom = 3;
                        }

                        $0.swiper.zoom.in(zoom);
                        };

                      addEventListener('wheel', zoomListener);
                      """,
              swiper.getElement());

      loadChapter();

      Tracker tracker = dataService.getTracker(chapter.getMangaId());

      if (tracker.hasAniListId()) {
        swiper.addActiveIndexChangeEventListener(
            e -> {
              if (e.getActiveIndex() == chapter.getPageCount() - 1) {
                log.info("Last page of chapter {}", chapter.getChapterNumber());
                trackerExecutor.submit(
                    () ->
                        trackingCommunicationService.setChapterProgress(
                            chapter.getMangaId(), chapter.getChapterNumber(), true));
                e.unregisterListener();
              }
            });
      }

      swiper.addReachEndEventListener(
          e -> {
            if (mangaService.setChapterRead(chapter.getId())) {
              log.info("Set chapter {} to read", chapter.getName());
            } else {
              log.warn("Couldn't set chapter {} to read", chapter.getName());
            }
          });

      add(swiper);
    }

    private void loadChapter() {

      var urls = mangaService.getChapterPages(chapter.getId());

      Settings settings = settingsService.getSettings();
      String baseUrl = settings.getUrl();

      for (int i = 0; i < urls.size(); i++) {
        String url = baseUrl + urls.get(i);

        Image image = new Image(url, "Page %d".formatted(i + 1));

        if (i > 1) {
          image.getElement().setAttribute("loading", "lazy");
        }

        image.addClassName("manga-page");

        swiper.addZoomable(true, image);
      }
    }
  }

  private class Controls extends Div {

    private final int pageCount;
    private final int mangaId;
    private final int chapterIndex;

    public Controls(Reader reader, Chapter chapter, int chapterIndex) {
      addClassName("controls");

      int tempPageCount = chapter.getPageCount();

      if (tempPageCount == -1) {
        tempPageCount = mangaService.getChapter(chapter.getId()).getPageCount();
      }

      this.pageCount = tempPageCount;
      this.mangaId = chapter.getMangaId();
      this.chapterIndex = chapterIndex;

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

      if (chapterIndex >= chapters.size() - 1) {
        return;
      }

      int chapterIndex = this.chapterIndex + 1;
      Chapter nextChapter = chapters.get(chapterIndex);

      int nextChapterId = nextChapter.getId();
      var event =
          new ReaderChapterChangeEvent(MangaReader.this, false, mangaId, nextChapterId, chapters);
      MangaReader.this.fireEvent(event);
    }

    private void prevPage(Swiper swiper) {

      if (swiper.getActiveIndex() != 0) {
        swiper.slidePrev();
        return;
      }

      if (this.chapterIndex <= 0) {
        return;
      }

      var prevChapter = chapters.get(chapterIndex - 1);

      int prevChapterId = prevChapter.getId();

      var event =
          new ReaderChapterChangeEvent(MangaReader.this, false, mangaId, prevChapterId, chapters);
      MangaReader.this.fireEvent(event);
    }
  }
}
