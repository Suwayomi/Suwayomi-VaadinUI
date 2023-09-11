package online.hatsunemiku.tachideskvaadinui.component.card;

import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;

@Slf4j
public class MangaCard extends Card {

  public MangaCard(Settings settings, Manga manga) {
    super(manga.getTitle(), settings.getUrl() + manga.getThumbnailUrl());

    String link = "/manga/" + manga.getId();
    this.getElement().setAttribute("href", link);
  }
}
