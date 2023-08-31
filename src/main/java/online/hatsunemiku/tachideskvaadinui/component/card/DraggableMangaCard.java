package online.hatsunemiku.tachideskvaadinui.component.card;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.card.data.MangaCategoryDragData;
import online.hatsunemiku.tachideskvaadinui.component.card.event.MangaCategoryUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.tab.event.CategoryTabHighlightEvent;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;

@Slf4j
public class DraggableMangaCard extends MangaCard implements DragSource<Card> {

  private final long mangaId;
  private Category category;

  public DraggableMangaCard(Settings settings, Manga manga, Category category) {
    super(settings, manga);
    this.mangaId = manga.getId();
    this.category = category;

    addDragStartListener(e -> {
      MangaCategoryDragData dragData = new MangaCategoryDragData(manga, this.category);
      e.setDragData(dragData);

      var event = new CategoryTabHighlightEvent(this, true, true);

      ComponentUtil.fireEvent(UI.getCurrent(), event);
    });

    addDragEndListener(e -> {
      var event = new CategoryTabHighlightEvent(this, true, false);

      ComponentUtil.fireEvent(UI.getCurrent(), event);
    });

    UI currentUI = UI.getCurrent();

    ComponentUtil.addListener(currentUI, MangaCategoryUpdateEvent.class, e -> {
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
  }
}
