/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.reader.paged.PagedReader;
import online.hatsunemiku.tachideskvaadinui.component.reader.strip.StripReader;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.ReaderSettingsChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderDirection;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.utils.NavigationUtils;
import online.hatsunemiku.tachideskvaadinui.view.RootView;
import org.jetbrains.annotations.NotNull;

/**
 * MangaReader is a class that represents the main component for reading manga. It uses a {@link
 * Reader} to display the manga pages and provides controls for navigating between pages and
 * chapters with the help of a {@link Sidebar} and {@link Controls}.
 */
@CssImport("./css/components/reader/manga-reader.css")
@Slf4j
public class MangaReader extends Div {

  private final SettingsService settingsService;
  private final MangaService mangaService;
  private final int chapterIndex;
  private final List<Chapter> chapters;

  /**
   * Constructs a {@link MangaReader} object.
   *
   * @param chapter The Chapter object representing the chapter being read.
   * @param settingsService The SettingsService object used for managing reader settings.
   * @param mangaService The MangaService object used for manga-related operations.
   * @param chapters The list of chapters in the manga
   */
  public MangaReader(
      Chapter chapter,
      SettingsService settingsService,
      MangaService mangaService,
      List<Chapter> chapters) {
    addClassName("manga-reader");

    this.settingsService = settingsService;
    this.mangaService = mangaService;
    this.chapterIndex = chapters.stream().map(Chapter::getId).toList().indexOf(chapter.getId());
    this.chapters = List.copyOf(chapters);

    Settings settings = settingsService.getSettings();
    var readerSettings = settings.getReaderSettings(chapter.getMangaId());

    // So I can update the direction to the new direction once the reader has been replaced
    AtomicReference<ReaderDirection> dir = new AtomicReference<>(readerSettings.getDirection());

    replaceReader(dir.get(), chapter);

    UI ui = getUI().orElseGet(UI::getCurrent);

    var settingsChangeListener =
        ComponentUtil.addListener(
            ui,
            ReaderSettingsChangeEvent.class,
            e -> {
              var newSettings = e.getNewSettings();
              var newDir = newSettings.getDirection();

              if (newDir == dir.get()) {
                return;
              }

              // if the new or old direction is vertical then the reader implementation must change
              // as both LTR and RTL use PagedReader, while only Vertical uses StripReader
              if (newDir == ReaderDirection.VERTICAL || dir.get() == ReaderDirection.VERTICAL) {
                var oldReader = (Reader) getComponentAt(1);
                int currentPageIndex = oldReader.getPageIndex();

                replaceReader(newDir, chapter);

                var newReader = (Reader) getComponentAt(1);
                newReader.moveToPage(currentPageIndex);

                dir.set(newDir);
              }
            });

    addDetachListener(e -> settingsChangeListener.remove());
  }

  /**
   * Creates a reader with the correct Implementation based on the given parameters.
   *
   * @param direction the {@link ReaderDirection direction} of the reader
   * @param chapter the {@link Chapter chapter} to be read
   * @return a Reader object of either {@link PagedReader} or {@link StripReader}
   */
  private Reader createReader(ReaderDirection direction, Chapter chapter) {
    Reader reader;

    if (direction == ReaderDirection.VERTICAL) {
      reader = new StripReader(chapter, mangaService, settingsService);
    } else {
      reader = new PagedReader(chapter, mangaService, settingsService);
    }

    return reader;
  }

  /**
   * Replaces the existing reader with a new one based on the specified direction and chapter. If no
   * reader exists, it will just add the new reader instead.
   *
   * @param direction the {@link ReaderDirection direction} of the new reader
   * @param chapter the {@link Chapter chapter} that should be displayed
   */
  private void replaceReader(ReaderDirection direction, Chapter chapter) {
    removeAll();

    var reader = createReader(direction, chapter);

    Sidebar sidebar = new Sidebar(mangaService, chapter, reader);
    Controls controls = new Controls(reader, chapter, chapterIndex);

    reader.addReaderReachEndListener(
        e -> {
          if (mangaService.setChapterRead(chapter.getId(), chapter.getMangaId())) {
            log.info("Set chapter {} to read", chapter.getName());
          } else {
            log.warn("Couldn't set chapter {} to read", chapter.getName());
          }

          e.unregisterListener();
        });

    add(sidebar, reader, controls);
  }

  public Registration addReaderChapterChangeEventListener(
      ComponentEventListener<ReaderChapterChangeEvent> listener) {
    return addListener(ReaderChapterChangeEvent.class, listener);
  }

  // skipcq: JAVA-W1019
  private class Sidebar extends Div {

    public Sidebar(MangaService mangaService, Chapter chapter, Reader reader) {
      addClassName("sidebar");

      Div navigationButtons = getNavigationButtons(chapter);

      List<Chapter> chapters = mangaService.getChapterList(chapter.getMangaId());

      if (chapters.isEmpty()) {
        chapters = mangaService.fetchChapterList(chapter.getMangaId());
      }

      Div chapterSelect = new Div();
      chapterSelect.setClassName("chapter-select");
      chapterSelect.getStyle().set("--vaadin-combo-box-overlay-width", "20vw");

      Button leftBtn = getChapterLeftBtn(reader, chapter);

      Select<Chapter> chapterSelector = getChapterSelector(chapter, chapters);

      Button rightBtn = getChapterRightBtn(reader, chapter);

      chapterSelect.add(leftBtn, chapterSelector, rightBtn);

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
    private Button getChapterRightBtn(Reader reader, Chapter chapter) {
      Button rightBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
      rightBtn.setId("rightBtn");
      rightBtn.addClickListener(
          e -> {
            int newChapterId;

            if (reader.getReaderDirection() == ReaderDirection.RTL) {

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
    private Select<Chapter> getChapterSelector(Chapter chapter, List<Chapter> chapters) {
      Select<Chapter> chapterSelector = new Select<>();
      chapterSelector.setId("chapter-selector");
      chapterSelector.setOverlayClassName("chapter-select-overlay");
      chapterSelector.setRenderer(createRenderer());
      chapterSelector.setItems(chapters);

      //Doesn't show the chapter correctly if it's not the same instance as in the list.
      Chapter thisChapter = chapters.stream().filter(c -> c.getId() == chapter.getId()).findFirst().orElse(null);
      chapterSelector.setValue(thisChapter);
      chapterSelector.addValueChangeListener(
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
      return chapterSelector;
    }

    @NotNull
    private Button getChapterLeftBtn(Reader reader, Chapter chapter) {
      Button leftBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
      leftBtn.setId("leftBtn");
      leftBtn.addClickListener(
          e -> {
            int newChapterId;

            int mangaId = chapter.getMangaId();
            if (reader.getReaderDirection() == ReaderDirection.RTL) {

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

    private ComponentRenderer<? extends Component, Chapter> createRenderer() {
      return new ComponentRenderer<Component, Chapter>(chapter -> {
        var div = new Div();
        div.setText(chapter.getName());
        return div;
      });
    }

    @NotNull
    private static Button getHomeButton() {
      Button home = new Button(VaadinIcon.HOME.create());
      home.setId("homeBtn");
      home.addClickListener(e -> UI.getCurrent().navigate(RootView.class));
      return home;
    }
  }

  /**
   * Controls class represents the controls used in a manga reader. It provides buttons and text
   * fields for navigating between pages and chapters of a manga.
   */
  private class Controls extends Div {

    private final int pageCount;
    private final int mangaId;
    private final int chapterIndex;

    /**
     * Represents a {@link Controls} object that provides navigation controls for a {@link
     * MangaReader}.
     *
     * @param reader The {@link Reader} component on which the controls will work on.
     * @param chapter The {@link Chapter} object representing the chapter being read.
     * @param chapterIndex The index of the chapter being read.
     */
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
            if (!e.isFromClient()) {
              return;
            }

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

            if (value == reader.getPageIndex()) {
              log.debug("Value is the same as active index");
              input.setValue(e.getOldValue());
              return;
            }

            if (value > pageCount || value < 1) {
              log.debug("Value is out of bounds");
              input.setValue(e.getOldValue());
              return;
            }

            reader.moveToPage(value - 1);
            log.debug("Value changed to {}", value);
          });

      Text totalChapters = new Text("/ " + pageCount);

      reader.addReaderPageIndexChangeListener(
          e -> {
            int activeIndex = e.getPageIndex() + 1;
            input.setValue(String.valueOf(activeIndex));
          });

      pageTrack.add(input, totalChapters);

      Button right = getNextButton(reader);

      add(left, pageTrack, right);
    }

    /**
     * Retrieves a button for navigating to the next page in the reader.
     *
     * @param reader The {@link Reader} component on which the button will perform actions.
     * @return The {@link Button} object.
     */
    @NotNull
    private Button getNextButton(Reader reader) {
      Icon arrowRight = VaadinIcon.ARROW_RIGHT.create();
      Button right = new Button(arrowRight);
      right.addClickListener(
          e -> {
            var direction = reader.getReaderDirection();

            if (direction == ReaderDirection.RTL) {
              prevPage(reader);
            } else {
              nextPage(reader);
            }
          });

      if (reader.getReaderDirection() == ReaderDirection.RTL) {
        right.addClickShortcut(Key.ARROW_RIGHT);
      } else {
        right.addClickShortcut(Key.ARROW_LEFT);
      }

      right.setIconAfterText(true);
      return right;
    }

    /**
     * Retrieves a button for navigating to the previous page in the reader.
     *
     * @param reader The {@link Reader} component on which the button will perform actions.
     * @return The {@link Button} object.
     */
    @NotNull
    private Button getPrevButton(Reader reader) {
      Icon arrowLeft = VaadinIcon.ARROW_LEFT.create();
      Button left = new Button(arrowLeft);
      left.addClickListener(
          e -> {
            if (reader.getReaderDirection() == ReaderDirection.RTL) {
              nextPage(reader);
            } else {
              prevPage(reader);
            }
          });

      if (reader.getReaderDirection() == ReaderDirection.RTL) {
        left.addClickShortcut(Key.ARROW_LEFT);
      } else {
        left.addClickShortcut(Key.ARROW_RIGHT);
      }
      return left;
    }

    private void nextPage(Reader reader) {

      if (reader.getPageIndex() != pageCount - 1) {
        reader.moveToNextPage();
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

    private void prevPage(Reader reader) {

      if (reader.getPageIndex() != 0) {
        reader.moveToPreviousPage();
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
