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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import online.hatsunemiku.tachideskvaadinui.view.SettingsView;
import org.jetbrains.annotations.NotNull;

/**
 * MangaReader is the primary component for reading manga, offering a glassmorphic UI.
 * It supports different reader modes (Paged and Strip) and includes a Focus Mode to hide controls.
 */
@CssImport("./css/components/reader/manga-reader.css")
@Slf4j
public class MangaReader extends Div {

  private final SettingsService settingsService;
  private final MangaService mangaService;
  private final int chapterIndex;
  private final List<Chapter> chapters;
  private boolean focusMode = false;

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

    if (chapter.getManga() == null) {
      chapter.setManga(mangaService.getManga(chapter.getMangaId()));
    }

    Settings settings = settingsService.getSettings();
    var readerSettings = settings.getReaderSettings(chapter.getMangaId());

    AtomicReference<ReaderDirection> dir = new AtomicReference<>(readerSettings.getDirection());

    replaceReader(dir.get(), chapter);

    // Toggle focus mode on click, but ignore clicks on UI components and overlays
    getElement().addEventListener("click", e -> {
      focusMode = !focusMode;
      if (focusMode) {
        addClassName("focus-mode");
      } else {
        removeClassName("focus-mode");
      }
    }).setFilter("!(event.composedPath().some(el => el.classList && (el.classList.contains('sidebar') || el.classList.contains('controls'))) || " +
               "event.composedPath().some(el => el.tagName && el.tagName.includes('OVERLAY')))");

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

              var oldReader = (Reader) getComponentAt(0);
              int currentPageIndex = oldReader.getPageIndex();

              replaceReader(newDir, chapter);

              var newReader = (Reader) getComponentAt(0);
              newReader.moveToPage(currentPageIndex);

              dir.set(newDir);
            });

    addDetachListener(e -> settingsChangeListener.remove());
  }

  private Reader createReader(ReaderDirection direction, Chapter chapter) {
    if (direction == ReaderDirection.VERTICAL) {
      return new StripReader(chapter, mangaService, settingsService);
    } else {
      return new PagedReader(chapter, mangaService, settingsService);
    }
  }

  private void replaceReader(ReaderDirection direction, Chapter chapter) {
    removeAll();

    var reader = createReader(direction, chapter);
    Sidebar sidebar = new Sidebar(chapter);
    Controls controls = new Controls(reader, chapter, chapterIndex);

    reader.addReaderReachEndListener(
        e -> {
          if (mangaService.setChapterRead(chapter.getId(), chapter.getMangaId())) {
            log.info("Set chapter {} to read", chapter.getName());
          }
          e.unregisterListener();
        });

    add(reader, sidebar, controls);
  }

  public Registration addReaderChapterChangeEventListener(
      ComponentEventListener<ReaderChapterChangeEvent> listener) {
    return addListener(ReaderChapterChangeEvent.class, listener);
  }

  private class Sidebar extends Div {
    public Sidebar(Chapter chapter) {
      setClassName("sidebar");
      getElement().addEventListener("click", e -> {}).addEventData("event.stopPropagation()");

      Div navButtons = new Div();
      navButtons.addClassName("navigation-buttons");

      Button home = new Button(VaadinIcon.HOME.create(), e -> UI.getCurrent().navigate(RootView.class));
      home.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      home.setTooltipText("Home");

      Button back = new Button(VaadinIcon.BOOK.create(), e -> NavigationUtils.navigateToManga(chapter.getMangaId(), UI.getCurrent()));
      back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      back.setTooltipText("Back to Manga");

      navButtons.add(home, back);

      Button settingsBtn = new Button(VaadinIcon.COG.create(), e -> UI.getCurrent().navigate(SettingsView.class));
      settingsBtn.setId("settings-btn");
      settingsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      settingsBtn.setTooltipText("Global Settings");

      add(navButtons, settingsBtn);
    }
  }

  private class Controls extends Div {
    private final int pageCount;

    public Controls(Reader reader, Chapter chapter, int chapterIndex) {
      setClassName("controls");
      getElement().addEventListener("click", e -> {}).addEventData("event.stopPropagation()");
      this.pageCount = chapter.getPageCount() != -1 ? chapter.getPageCount() : mangaService.getChapter(chapter.getId()).getPageCount();

      // Left: Manga Info
      Div mangaInfo = new Div();
      mangaInfo.setClassName("manga-info");
      Span title = new Span(chapter.getManga() != null ? chapter.getManga().getTitle() : "Manga");
      title.setClassName("manga-title");
      Span chapterName = new Span(chapter.getName().toUpperCase());
      chapterName.setClassName("chapter-info");
      mangaInfo.add(title, chapterName);

      // Center-Left: Chapter Navigation
      Div navGroup = new Div();
      navGroup.setClassName("nav-group");
      
      Button leftBtn = new Button();
      Button rightBtn = new Button();
      leftBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      rightBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

      if (reader.getReaderDirection() == ReaderDirection.RTL) {
          // RTL: Left is Next Chapter, Right is Prev Chapter
          leftBtn.setText("Next");
          leftBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
          leftBtn.addClickListener(e -> nextChapter());

          rightBtn.setText("Prev");
          rightBtn.setIcon(VaadinIcon.ARROW_RIGHT.create());
          rightBtn.setIconAfterText(true);
          rightBtn.addClickListener(e -> prevChapter());
      } else {
          // LTR/Vertical: Left is Prev Chapter, Right is Next Chapter
          leftBtn.setText("Prev");
          leftBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
          leftBtn.addClickListener(e -> prevChapter());

          rightBtn.setText("Next");
          rightBtn.setIcon(VaadinIcon.ARROW_RIGHT.create());
          rightBtn.setIconAfterText(true);
          rightBtn.addClickListener(e -> nextChapter());
      }
      
      navGroup.add(leftBtn, rightBtn);

      // Center: Page & Chapter Controls
      Div pageControls = new Div();
      pageControls.setClassName("page-controls");

      Div pageIndicator = new Div();
      pageIndicator.setClassName("page-indicator");
      
      Button pageLeft = new Button(VaadinIcon.CHEVRON_LEFT.create());
      Button pageRight = new Button(VaadinIcon.CHEVRON_RIGHT.create());
      pageLeft.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      pageRight.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

      TextField input = new TextField();
      input.setValue("1");
      input.setAllowedCharPattern("\\d");
      input.addValueChangeListener(e -> {
        if (e.isFromClient() && !e.getValue().isEmpty()) {
          int val = Integer.parseInt(e.getValue());
          if (val >= 1 && val <= pageCount) reader.moveToPage(val - 1);
          else input.setValue(e.getOldValue());
        }
      });

      if (reader.getReaderDirection() == ReaderDirection.RTL) {
          // RTL Page Logic: Left is Next Page (with boundary jump), Right is Prev Page
          pageLeft.addClickListener(e -> nextPage(reader));
          pageLeft.addClickShortcut(Key.ARROW_LEFT);
          pageRight.addClickListener(e -> prevPage(reader));
          pageRight.addClickShortcut(Key.ARROW_RIGHT);
      } else {
          // LTR Page Logic: Left is Prev Page, Right is Next Page
          pageLeft.addClickListener(e -> prevPage(reader));
          pageLeft.addClickShortcut(Key.ARROW_LEFT);
          pageRight.addClickListener(e -> nextPage(reader));
          pageRight.addClickShortcut(Key.ARROW_RIGHT);
      }

      pageIndicator.add(pageLeft, input, new Span("/ " + pageCount), pageRight);

      List<Chapter> chapterList = mangaService.getChapterList(chapter.getMangaId());
      if (chapterList.isEmpty()) {
        chapterList = mangaService.fetchChapterList(chapter.getMangaId());
      }
      Select<Chapter> selector = new Select<>();
      selector.setItems(chapterList);
      selector.setValue(chapterList.stream().filter(c -> c.getId() == chapter.getId()).findFirst().orElse(null));
      selector.setRenderer(new ComponentRenderer<>(c -> {
        Span span = new Span(c.getName());
        span.getStyle().set("font-family", "Space Grotesk");
        return span;
      }));
      selector.addValueChangeListener(e -> {
        if (e.isFromClient() && e.getValue() != null && !Objects.equals(e.getOldValue(), e.getValue())) {
          MangaReader.this.fireEvent(new ReaderChapterChangeEvent(MangaReader.this, false, e.getValue().getMangaId(), e.getValue().getId(), chapters));
        }
      });

      pageControls.add(pageIndicator, selector);

      // Right: Actions
      Div actionGroup = new Div();
      actionGroup.setClassName("action-group");
      Button settingsBtn = new Button(VaadinIcon.COG.create(), e -> new ReaderSettingsDialog(settingsService.getSettings(), chapter.getMangaId()).open());
      settingsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      settingsBtn.setTooltipText("Reader Settings");
      
      Button fullscreenBtn = new Button(VaadinIcon.EXPAND_SQUARE.create());
      fullscreenBtn.addClickListener(e -> {
          UI.getCurrent().getPage().executeJs("if (!document.fullscreenElement) { document.documentElement.requestFullscreen(); } else { if (document.exitFullscreen) { document.exitFullscreen(); } }");
      });
      fullscreenBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
      fullscreenBtn.setTooltipText("Toggle Fullscreen");
      fullscreenBtn.getElement().executeJs(
          "const btn = $0; " +
          "const icon = btn.querySelector('vaadin-icon'); " +
          "const update = () => { " +
          "  if (document.fullscreenElement) { " +
          "    icon.setAttribute('icon', 'vaadin:compress-square'); " +
          "    btn.setAttribute('title', 'Exit Fullscreen'); " +
          "  } else { " +
          "    icon.setAttribute('icon', 'vaadin:expand-square'); " +
          "    btn.setAttribute('title', 'Toggle Fullscreen'); " +
          "  } " +
          "}; " +
          "document.addEventListener('fullscreenchange', update); " +
          "update();", fullscreenBtn.getElement());
      
      actionGroup.add(settingsBtn, fullscreenBtn);

      add(mangaInfo, navGroup, pageControls, actionGroup);

      // Initialize Progress
      int initialPage = reader.getPageIndex() + 1;
      double initialProgress = ((double) initialPage / pageCount) * 100;
      getElement().getStyle().set("--reader-progress", initialProgress + "%");

      // Progress Update
      reader.addReaderPageIndexChangeListener(e -> {
        input.setValue(String.valueOf(e.getPageIndex() + 1));
        double progress = ((double) (e.getPageIndex() + 1) / pageCount) * 100;
        getElement().getStyle().set("--reader-progress", progress + "%");
      });
    }

    private void nextPage(Reader reader) {
      if (reader.getPageIndex() < pageCount - 1) {
        reader.moveToNextPage();
      } else {
        nextChapter();
      }
    }

    private void prevPage(Reader reader) {
      if (reader.getPageIndex() > 0) {
        reader.moveToPreviousPage();
      } else {
        prevChapter();
      }
    }

    private void nextChapter() {
      if (chapterIndex < chapters.size() - 1) {
        MangaReader.this.fireEvent(new ReaderChapterChangeEvent(MangaReader.this, false, chapters.get(chapterIndex + 1).getMangaId(), chapters.get(chapterIndex + 1).getId(), chapters));
      }
    }

    private void prevChapter() {
      if (chapterIndex > 0) {
        MangaReader.this.fireEvent(new ReaderChapterChangeEvent(MangaReader.this, false, chapters.get(chapterIndex - 1).getMangaId(), chapters.get(chapterIndex - 1).getId(), chapters));
      }
    }
  }
}

