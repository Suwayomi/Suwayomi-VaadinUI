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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

    Div background = getChapterBackgroundDiv(chapter);
    container.add(background);

    Div title = new Div();

    if (chapter.getChapterNumber() == (int) chapter.getChapterNumber()) {
      title.setText("Chapter " + (int) chapter.getChapterNumber());
    } else {
      title.setText("Chapter " + chapter.getChapterNumber());
    }

    title.setClassName("chapter-list-box-item-title");

    long dateLong = chapter.getUploadDate();

    String formattedDate;

    if (dateLong == 0) {
      formattedDate = "Today";
    } else {
      Date uploadDate = new Date(chapter.getUploadDate());
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
      formattedDate = formatter.format(uploadDate);
    }

    Div date = new Div();
    date.setText(formattedDate);
    date.setClassName("chapter-list-box-item-date");

    Div rightSide = new Div();
    rightSide.addClassName("chapter-list-box-item-right-side");

    Button readStatusBtn;
    if (!chapter.isRead()) {
      readStatusBtn = getReadButton(chapter, mangaService, rightSide);
    } else {
      addReadStatus(container);
      readStatusBtn = getUnreadButton(chapter, mangaService, rightSide);
    }

    Button downloadBtn = getDownloadBtn(chapter, mangaService, rightSide);

    rightSide.add(readStatusBtn, downloadBtn, date);

    container.add(title, rightSide);

    ComponentUtil.addListener(
        rightSide,
        ChapterReadStatusChangeEvent.class,
        e -> {
          log.debug("ChapterReadStatusChangeEvent received");
          if (e.isRead()) {
            addReadStatus(container);
          } else {
            removeReadStatus(container);
          }
        });

    var ui = container.getUI().orElse(UI.getCurrent());

    ComponentUtil.addListener(
        ui,
        ChapterReadSyncEvent.class,
        e -> {
          if (e.getChapterNumbers().contains(chapter.getChapterNumber())) {
            addReadStatus(container);
            var readBtn =
                rightSide
                    .getChildren()
                    .filter(btn -> btn instanceof Button)
                    .filter(btn -> btn.getId().orElse("").equals("read-button"))
                    .findFirst()
                    .orElseThrow();

            rightSide.replace(readBtn, getUnreadButton(chapter, mangaService, rightSide));
          }
        });

    return container;
  }

  @NotNull
  private static Button getDownloadBtn(Chapter chapter, MangaService mangaService, Div rightSide) {
    if (chapter.isDownloaded()) {
      Button deleteBtn = new Button(VaadinIcon.TRASH.create());
      deleteBtn.addClickListener(
          e -> {
            var success = mangaService.deleteSingleChapter(chapter.getId());

            Notification notification;

            if (!success) {
              log.error("Failed to delete chapter");
              notification = new Notification("Failed to delete chapter", 5000);
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
              notification = new Notification("Deleting chapter", 5000);
              notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

              Chapter chapterCopy = chapter.withDownloaded(false);
              rightSide.replace(deleteBtn, getDownloadBtn(chapterCopy, mangaService, rightSide));
            }

            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
          });
      return deleteBtn;
    } else {
      Button downloadBtn = new Button(VaadinIcon.DOWNLOAD.create());
      downloadBtn.addClickListener(
          e -> {
            var success = mangaService.downloadSingleChapter(chapter.getId());

            Notification notification;

            if (!success) {
              log.error("Failed to download chapter");
              notification = new Notification("Failed to download chapter", 5000);
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
              notification = new Notification("Downloading chapter", 5000);
              notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
              downloadBtn.setEnabled(false);
              downloadBtn.addClassName("downloading");

              UI ui = UI.getCurrent();

              trackChapterDownload(chapter, mangaService, rightSide, ui, downloadBtn);
            }

            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
          });

      UI ui = UI.getCurrent();

      ComponentUtil.addListener(
          ui,
          DownloadAllChapterEvent.class,
          e -> {
            downloadBtn.setEnabled(false);
            downloadBtn.addClassName("downloading");
            trackChapterDownload(chapter, mangaService, rightSide, ui, downloadBtn);
          });

      return downloadBtn;
    }
  }

  private static void trackChapterDownload(
      Chapter chapter, MangaService mangaService, Div rightSide, UI ui, Button downloadBtn) {
    mangaService.addDownloadTrackListener(
        chapter.getId(),
        () -> {
          if (!ui.isAttached()) {
            return;
          }

          ui.access(
              () -> {
                Chapter chapterCopy = chapter.withDownloaded(true);
                Button deleteBtn = getDownloadBtn(chapterCopy, mangaService, rightSide);
                rightSide.replace(downloadBtn, deleteBtn);
              });
        });
  }

  @NotNull
  private static Div getChapterBackgroundDiv(Chapter chapter) {
    Div background = new Div();
    background.addClickListener(
        e -> {
          int mangaId = chapter.getMangaId();

          RouteParam mangaIdParam = new RouteParam("mangaId", String.valueOf(mangaId));
          RouteParam chapterIdParam = new RouteParam("chapterId", String.valueOf(chapter.getId()));

          RouteParameters params = new RouteParameters(mangaIdParam, chapterIdParam);

          UI.getCurrent().navigate(ReadingView.class, params);
        });
    background.setClassName("chapter-list-box-item-background");
    return background;
  }

  /**
   * Creates a read button for the given chapter.
   *
   * @param chapter The chapter to create the read button for
   * @param mangaService The manga service to use for setting the chapter read
   * @param rightSide The right side div the button is in for replacement
   * @return The read {@link Button button}
   */
  private static Button getReadButton(Chapter chapter, MangaService mangaService, Div rightSide) {
    Button readButton = new Button(VaadinIcon.EYE.create());
    readButton.setId("read-button");
    readButton.addClickListener(
        e -> {
          if (!mangaService.setChapterRead(chapter.getId(), chapter.getMangaId())) {
            log.error("Failed to set chapter read");
            Notification notification = new Notification("Failed to set chapter read", 5000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
            return;
          }

          Button unreadBtn = getUnreadButton(chapter, mangaService, rightSide);
          rightSide.replace(readButton, unreadBtn);

          var readEvent = new ChapterReadStatusChangeEvent(readButton, true, true);
          ComponentUtil.fireEvent(rightSide, readEvent);
        });

    return readButton;
  }

  /**
   * Creates an unread button for the given chapter.
   *
   * @param chapter The chapter to create the unread button for
   * @param mangaService The manga service to use for setting the chapter unread
   * @param rightSide The right side div the button is in for replacement
   * @return The unread {@link Button button}
   */
  private static Button getUnreadButton(Chapter chapter, MangaService mangaService, Div rightSide) {
    Button unreadButton = new Button(VaadinIcon.EYE_SLASH.create());
    unreadButton.setId("unread-button");
    unreadButton.addClickListener(
        e -> {
          if (!mangaService.setChapterUnread(chapter.getId())) {
            log.error("Failed to set chapter unread");
            Notification notification = new Notification("Failed to set chapter unread", 5000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
            return;
          }
          Button readBtn = getReadButton(chapter, mangaService, rightSide);
          rightSide.replace(unreadButton, readBtn);

          var readEvent = new ChapterReadStatusChangeEvent(unreadButton, true, false);
          ComponentUtil.fireEvent(rightSide, readEvent);
        });

    return unreadButton;
  }

  private static void addReadStatus(HorizontalLayout container) {
    container.addClassName("chapter-list-box-item-read");
  }

  private static void removeReadStatus(HorizontalLayout container) {
    container.removeClassName("chapter-list-box-item-read");
  }
}
