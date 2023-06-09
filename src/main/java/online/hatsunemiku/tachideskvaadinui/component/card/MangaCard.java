package online.hatsunemiku.tachideskvaadinui.component.card;

import com.vaadin.flow.router.RouteParameters;
import online.hatsunemiku.tachideskvaadinui.data.Manga;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.view.MangaView;

public class MangaCard extends Card {

  public MangaCard(Settings settings, Manga manga) {
    super(manga.getTitle(), settings.getUrl() + manga.getThumbnailUrl());
    addClickListener(e -> {
      RouteParameters params = new RouteParameters("id", String.valueOf(manga.getId()));

      getUI().ifPresent(ui -> ui.navigate(MangaView.class, params));
    });
  }

}
