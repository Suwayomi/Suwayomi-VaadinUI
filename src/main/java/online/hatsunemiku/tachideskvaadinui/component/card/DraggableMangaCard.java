/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.card;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.card.data.MangaCategoryDragData;
import online.hatsunemiku.tachideskvaadinui.component.card.event.MangaCategoryUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.tab.event.CategoryTabHighlightEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;

@Slf4j
public class DraggableMangaCard extends MangaCard implements DragSource<Card> {

  private final long mangaId;
  private Category category;
  private final MangaService mangaService;

  public DraggableMangaCard(Settings settings, Manga manga, Category category,
      MangaService mangaService) {
    super(settings, manga);
    this.mangaId = manga.getId();
    this.category = category;
    this.mangaService = mangaService;

    addDragStartListener(
        e -> {
          MangaCategoryDragData dragData = new MangaCategoryDragData(manga, this.category);
          e.setDragData(dragData);

          var event = new CategoryTabHighlightEvent(this, true, true);

          ComponentUtil.fireEvent(UI.getCurrent(), event);
        });

    addDragEndListener(
        e -> {
          var event = new CategoryTabHighlightEvent(this, true, false);

          ComponentUtil.fireEvent(UI.getCurrent(), event);
        });

    UI currentUI = UI.getCurrent();

    ComponentUtil.addListener(
        currentUI,
        MangaCategoryUpdateEvent.class,
        e -> {
          if (this.mangaId == e.getMangaId()) {
            this.category = e.getNewCategory();
            removeFromParent();

            var tab = e.getSource();
            Div grid = tab.getGrid();

            if (grid == null) {
              getUI().ifPresent(ui -> ui.getPage().reload());
            } else {
              grid.add(this);
            }
          }
        });

    setEffectAllowed(EffectAllowed.MOVE);
    setDraggable(true);

    ContextMenu contextMenu = new ContextMenu();
    contextMenu.setTarget(this);
    contextMenu.addItem(
        "Remove from library",
        e -> {
          boolean success = this.mangaService.removeMangaFromLibrary((int) this.mangaId);
          if (success) {
            Notification notification = new Notification("Removed manga from library", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
            removeFromParent();
          } else {
            Notification notification = new Notification("Failed to remove manga from library",
                3000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
          }
        });
  }
}
