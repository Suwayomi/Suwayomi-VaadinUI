/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.tab;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.card.data.MangaCategoryDragData;
import online.hatsunemiku.tachideskvaadinui.component.card.event.MangaCategoryUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.tab.event.CategoryTabHighlightEvent;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;

@Getter
@Slf4j
@CssImport("./css/components/tab/category-tab.css")
public class CategoryTab extends Tab implements DropTarget<Tab> {

  @Setter private Div grid;

  public CategoryTab(Category category, MangaService mangaService) {
    super(category.getName());

    addClassName("category-tab");

    addDropListener(
        e -> {
          var dragData = e.getDragData();

          if (dragData.isEmpty()) {
            return;
          }

          var obj = dragData.get();

          if (obj instanceof MangaCategoryDragData data) {
            Manga manga = data.manga();
            Category oldCategory = data.category();

            if (category.getId() == 0) {
              Notification notification = new Notification("Cannot move to Default", 3000);
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
              notification.open();
              return;
            }

            if (category.getId() == oldCategory.getId()) {
              Notification notification = new Notification("Cannot move to same category", 3000);
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
              notification.open();
              return;
            }

            mangaService.moveMangaToCategory(manga.getId(), category.getId(), oldCategory.getId());
            log.debug("Manga {} moved to category {}", manga.getTitle(), category.getName());

            long mangaId = manga.getId();

            var updateEvent = new MangaCategoryUpdateEvent(this, true, mangaId, category);
            var highlightEvent = new CategoryTabHighlightEvent(this, true, false);

            if (getUI().isEmpty()) {
              return;
            }

            log.debug("Firing update event for manga {}", manga.getTitle());

            ComponentUtil.fireEvent(getUI().get(), updateEvent);
            ComponentUtil.fireEvent(getUI().get(), highlightEvent);
          }
        });

    ComponentUtil.addListener(
        UI.getCurrent(),
        CategoryTabHighlightEvent.class,
        e -> {
          if (e.isHighlight()) {
            addClassName("drop-target");
          } else {
            removeClassName("drop-target");
          }
        });

    setDropEffect(DropEffect.MOVE);
    setActive(true);
  }
}
