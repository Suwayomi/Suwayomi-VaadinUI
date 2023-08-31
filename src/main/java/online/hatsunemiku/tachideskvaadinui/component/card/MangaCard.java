package online.hatsunemiku.tachideskvaadinui.component.card;

import com.vaadin.flow.router.RouteParameters;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.view.MangaView;

@Slf4j
public class MangaCard extends Card {

  public MangaCard(Settings settings, Manga manga) {
    super(manga.getTitle(), settings.getUrl() + manga.getThumbnailUrl());
    addClickListener(e -> {
      RouteParameters params = new RouteParameters("id", String.valueOf(manga.getId()));

      getUI().ifPresent(ui -> ui.navigate(MangaView.class, params));
    });
  }
}
