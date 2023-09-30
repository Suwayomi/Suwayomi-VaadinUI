package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist;

import java.util.List;

public record MangaList(List<AniListMedia> reading, List<AniListMedia> planToRead,
                        List<AniListMedia> completed, List<AniListMedia> onHold,
                        List<AniListMedia> dropped) {

}
