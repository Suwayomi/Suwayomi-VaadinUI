package online.hatsunemiku.tachideskvaadinui.data.tachidesk.event

import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga

class MangaUpdateEvent(val isRunning: Boolean, val completedJobs: List<Manga>)
