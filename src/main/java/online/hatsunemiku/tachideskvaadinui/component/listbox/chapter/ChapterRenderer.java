/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.listbox.chapter.event.ChapterReadStatusChangeEvent;
import online.hatsunemiku.tachideskvaadinui.component.listbox.chapter.event.ChapterReadSyncEvent;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.view.MangaView;
import online.hatsunemiku.tachideskvaadinui.view.MangaView.DownloadAllChapterEvent;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;
import org.jetbrains.annotations.NotNull;

/**
 * Renders a {@link Chapter} as a {@link HorizontalLayout} component for use in a list box. This is
 * used for the chapter list in the {@link MangaView}.
 */
@Slf4j
public class ChapterRenderer extends ComponentRenderer<HorizontalLayout, Chapter> {

  public ChapterRenderer(MangaService mangaService) {
    super(chapter -> createPresentation(chapter, mangaService));
  }

  /**
   * Creates the presentation of a chapter in a list box.
   *
   * @param chapter The chapter to render
   * @param mangaService The manga service to use for setting the chapter read status
   * @return The presentation of the chapter as a {@link HorizontalLayout}
   */
  private static HorizontalLayout createPresentation(Chapter chapter, MangaService mangaService) {
    HorizontalLayout container = new HorizontalLayout();
    container.addClassName("chapter-list-box-item");

    Div titleGroup = new Div();
    titleGroup.addClassName("chapter-list-box-item-title-group");

    Span title = new Span();
    if (chapter.getChapterNumber() == (int) chapter.getChapterNumber()) {
      title.setText("Chapter " + (int) chapter.getChapterNumber());
    } else {
      title.setText("Chapter " + chapter.getChapterNumber());
    }
    title.setClassName("chapter-list-box-item-title");

    titleGroup.add(title);

    if (chapter.getName() != null && !chapter.getName().isEmpty() && !chapter.getName().toLowerCase().contains("chapter")) {
      Span subtitle = new Span(chapter.getName());
      subtitle.setClassName("chapter-list-box-item-subtitle");
      titleGroup.add(subtitle);
    }

    container.add(titleGroup);

    Div rightSide = new Div();
    rightSide.addClassName("chapter-list-box-item-right-side");

    long dateLong = chapter.getUploadDate();
    String formattedDate;
    if (dateLong == 0) {
      formattedDate = "Today";
    } else {
      Date uploadDate = new Date(chapter.getUploadDate());
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
      formattedDate = formatter.format(uploadDate);
    }

    Div dateDiv = new Div();
    dateDiv.setText(formattedDate);
    dateDiv.setClassName("chapter-list-box-item-date");

    Div actions = new Div();
    actions.addClassName("chapter-list-box-item-actions");

    Button readStatusBtn;
    if (!chapter.isRead()) {
      readStatusBtn = getReadButton(chapter, mangaService, actions);
    } else {
      container.addClassName("chapter-list-box-item-read");
      readStatusBtn = getUnreadButton(chapter, mangaService, actions);
    }

    Button downloadBtn = getDownloadBtn(chapter, mangaService, actions);

    actions.add(readStatusBtn, downloadBtn);
    rightSide.add(dateDiv, actions);

    container.add(rightSide);

    // Click listener for the whole row to navigate
    container.addClickListener(e -> {
      int mangaId = chapter.getMangaId();
      RouteParam mangaIdParam = new RouteParam("mangaId", String.valueOf(mangaId));
      RouteParam chapterIdParam = new RouteParam("chapterId", String.valueOf(chapter.getId()));
      RouteParameters params = new RouteParameters(mangaIdParam, chapterIdParam);
      UI.getCurrent().navigate(ReadingView.class, params);
    });

    ComponentUtil.addListener(
        actions,
        ChapterReadStatusChangeEvent.class,
        e -> {
          if (e.isRead()) {
            container.addClassName("chapter-list-box-item-read");
          } else {
            container.removeClassName("chapter-list-box-item-read");
          }
        });

    var ui = container.getUI().orElse(UI.getCurrent());
    ComponentUtil.addListener(
        ui,
        ChapterReadSyncEvent.class,
        e -> {
          if (e.getChapterNumbers().contains(chapter.getChapterNumber())) {
            container.addClassName("chapter-list-box-item-read");
            var optional =
                actions
                    .getChildren()
                    .filter(btn -> btn instanceof Button)
                    .filter(btn -> btn.getId().orElse("").equals("read-button"))
                    .findFirst();

            if (optional.isPresent()) {
              var readBtn = (Button) optional.get();
              actions.replace(readBtn, getUnreadButton(chapter, mangaService, actions));
            }
          }
        });

    return container;
  }

  @NotNull
  private static Button getDownloadBtn(Chapter chapter, MangaService mangaService, Div actions) {
    if (chapter.isDownloaded()) {
      Button deleteBtn = new Button(VaadinIcon.TRASH.create());
      deleteBtn.getElement().addEventListener("click", e -> {}).addEventData("event.stopPropagation()");
      deleteBtn.addClickListener(
          e -> {
            var success = mangaService.deleteSingleChapter(chapter.getId());
            if (success) {
              Chapter chapterCopy = chapter.withDownloaded(false);
              actions.replace(deleteBtn, getDownloadBtn(chapterCopy, mangaService, actions));
            }
          });
      return deleteBtn;
    } else {
      Button downloadBtn = new Button(VaadinIcon.DOWNLOAD.create());
      downloadBtn.getElement().addEventListener("click", e -> {}).addEventData("event.stopPropagation()");
      downloadBtn.addClickListener(
          e -> {
            var success = mangaService.downloadSingleChapter(chapter.getId());
            if (success) {
              downloadBtn.setEnabled(false);
              downloadBtn.addClassName("downloading");
              UI ui = UI.getCurrent();
              trackChapterDownload(chapter, mangaService, actions, ui, downloadBtn);
            }
          });

      UI ui = UI.getCurrent();
      ComponentUtil.addListener(
          ui,
          DownloadAllChapterEvent.class,
          e -> {
            downloadBtn.setEnabled(false);
            downloadBtn.addClassName("downloading");
            trackChapterDownload(chapter, mangaService, actions, ui, downloadBtn);
          });

      return downloadBtn;
    }
  }

  private static void trackChapterDownload(
      Chapter chapter, MangaService mangaService, Div actions, UI ui, Button downloadBtn) {
    mangaService.addDownloadTrackListener(
        chapter.getId(),
        () -> {
          if (ui.isAttached()) {
            ui.access(
                () -> {
                  Chapter chapterCopy = chapter.withDownloaded(true);
                  Button deleteBtn = getDownloadBtn(chapterCopy, mangaService, actions);
                  actions.replace(downloadBtn, deleteBtn);
                });
          }
        });
  }

  /**
   * Creates a read button for the given chapter.
   *
   * @param chapter The chapter to create the read button for
   * @param mangaService The manga service to use for setting the chapter read
   * @param actions The actions div the button is in for replacement
   * @return The read {@link Button button}
   */
  private static Button getReadButton(Chapter chapter, MangaService mangaService, Div actions) {
    Button readButton = new Button(VaadinIcon.EYE.create());
    readButton.getElement().addEventListener("click", e -> {}).addEventData("event.stopPropagation()");
    readButton.setId("read-button");
    readButton.addClickListener(
        e -> {
          if (mangaService.setChapterRead(chapter.getId(), chapter.getMangaId())) {
            Button unreadBtn = getUnreadButton(chapter, mangaService, actions);
            actions.replace(readButton, unreadBtn);
            var readEvent = new ChapterReadStatusChangeEvent(readButton, true, true);
            ComponentUtil.fireEvent(actions, readEvent);
          }
        });

    return readButton;
  }

  /**
   * Creates an unread button for the given chapter.
   *
   * @param chapter The chapter to create the unread button for
   * @param mangaService The manga service to use for setting the chapter unread
   * @param actions The actions div the button is in for replacement
   * @return The unread {@link Button button}
   */
  private static Button getUnreadButton(Chapter chapter, MangaService mangaService, Div actions) {
    Button unreadButton = new Button(VaadinIcon.EYE_SLASH.create());
    unreadButton.getElement().addEventListener("click", e -> {}).addEventData("event.stopPropagation()");
    unreadButton.setId("unread-button");
    unreadButton.addClickListener(
        e -> {
          if (mangaService.setChapterUnread(chapter.getId())) {
            Button readBtn = getReadButton(chapter, mangaService, actions);
            actions.replace(unreadButton, readBtn);
            var readEvent = new ChapterReadStatusChangeEvent(unreadButton, true, false);
            ComponentUtil.fireEvent(actions, readEvent);
          }
        });

    return unreadButton;
  }
}
