package online.hatsunemiku.tachideskvaadinui.data.tachidesk.search;

import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;

public record SearchResponse(List<Manga> mangaList, boolean hasNext) {

}
