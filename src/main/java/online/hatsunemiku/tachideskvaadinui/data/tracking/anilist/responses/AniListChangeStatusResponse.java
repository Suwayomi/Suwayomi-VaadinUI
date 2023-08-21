package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses;

import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;

public record AniListChangeStatusResponse(long id, AniListStatus status) {

}
