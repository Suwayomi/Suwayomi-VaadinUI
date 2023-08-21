package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.responses;

import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListStatus;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;

public record AniListMangaStatistics(
    AniListStatus status, int progress, int score, MediaDate startedAt, MediaDate completedAt) {}
