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
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ChapterRenderer extends ComponentRenderer<HorizontalLayout, Chapter> {

  public ChapterRenderer(MangaService mangaService) {
    super(chapter -> createPresentation(chapter, mangaService));
  }

  private static HorizontalLayout createPresentation(Chapter chapter, MangaService mangaService) {
    HorizontalLayout container = new HorizontalLayout();
    container.addClassName("chapter-list-box-item");

    Div background = getChapterBackgroundDiv(chapter);
    container.add(background);

    Div title = new Div();
    title.setText("Chapter " + chapter.getChapterNumber());
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

    rightSide.add(readStatusBtn, date);

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

    return container;
  }

  @NotNull
  private static Div getChapterBackgroundDiv(Chapter chapter) {
    Div background = new Div();
    background.addClickListener(
        e -> {
          int mangaId = chapter.getMangaId();

          RouteParam mangaIdParam = new RouteParam("mangaId", String.valueOf(mangaId));

          double chapterNumber = chapter.getIndex();
          RouteParam chapterIndexParam;
          if (chapterNumber % 1 == 0) {
            chapterIndexParam = new RouteParam("chapterIndex", String.valueOf((int) chapterNumber));
          } else {
            chapterIndexParam = new RouteParam("chapterIndex", String.valueOf(chapterNumber));
          }

          RouteParameters params = new RouteParameters(mangaIdParam, chapterIndexParam);

          UI.getCurrent().navigate(ReadingView.class, params);
        });
    background.setClassName("chapter-list-box-item-background");
    return background;
  }

  private static Button getReadButton(Chapter chapter, MangaService mangaService, Div rightSide) {
    Button readButton = new Button(VaadinIcon.EYE.create());
    readButton.addClickListener(
        e -> {
          if (!mangaService.setChapterRead(chapter.getMangaId(), chapter.getIndex())) {
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

  private static Button getUnreadButton(Chapter chapter, MangaService mangaService, Div rightSide) {
    Button unreadButton = new Button(VaadinIcon.EYE_SLASH.create());
    unreadButton.addClickListener(
        e -> {
          if (!mangaService.setChapterUnread(chapter.getMangaId(), chapter.getIndex())) {
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
