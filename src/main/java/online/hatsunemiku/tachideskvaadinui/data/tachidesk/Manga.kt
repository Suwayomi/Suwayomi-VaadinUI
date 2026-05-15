/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a manga object from the Suwayomi API.
 *
 * @since 0.9.0
 * @version 1.12.0
 */
class Manga {

    @JsonProperty("sourceId")
    var sourceId: String? = null

    @JsonProperty("artist")
    var artist: String? = null

    @JsonProperty("chaptersLastFetchedAt")
    var chaptersLastFetchedAt: Int = 0

    @JsonProperty("description")
    var description: String? = null

    @JsonProperty("unreadCount")
    var unreadCount: Int = 0

    @JsonProperty("source")
    var source: Source? = null

    @JsonProperty("title")
    var title: String? = null

    @JsonProperty("freshData")
    var isFreshData: Boolean = false

    @JsonProperty("thumbnailUrlLastFetched")
    var thumbnailUrlLastFetched: Int = 0

    @JsonProperty("inLibraryAt")
    var inLibraryAt: Int = 0

    @JsonProperty("genre")
    var genre: List<String>? = null

    @JsonProperty("realUrl")
    var realUrl: String? = null

    @JsonProperty("initialized")
    var isInitialized: Boolean = false

    @JsonProperty("id")
    var id: Int = 0

    @JsonProperty("thumbnailUrl")
    var thumbnailUrl: String? = null

    @JsonProperty("lastFetchedAt")
    var lastFetchedAt: Int = 0

    @JsonProperty("inLibrary")
    var isInLibrary: Boolean = false

    @JsonProperty("author")
    var author: String? = null

    @JsonProperty("chapterCount")
    var chapterCount: Int = 0

    @JsonProperty("url")
    var url: String? = null

    @JsonProperty("updateStrategy")
    var updateStrategy: String? = null

    @JsonProperty("chaptersAge")
    var chaptersAge: Int = 0

    @JsonProperty("lastChapterRead")
    var lastChapterRead: Chapter? = null

    @JsonProperty("downloadCount")
    var downloadCount: Int = 0

    @JsonProperty("age")
    var age: Int = 0

    @JsonProperty("status")
    var status: String? = null

    @JsonProperty("chapters")
    private var chapters: Chapters? = null

    @JsonProperty("categories")
    private var categories: MangaCategories? = null

    fun getLastChapterId(): Int {
        val edges = chapters?.edge ?: return 0
        if (edges.isEmpty()) return 0
        return edges.last().node?.id ?: 0
    }

    fun getFirstChapterId(): Int {
        val edges = chapters?.edge ?: return 0
        if (edges.isEmpty()) return 0
        return edges.first().node?.id ?: 0
    }

    fun getMangaCategories(): List<Category> {
        return categories?.nodes ?: emptyList()
    }

    /** Represents chapters of a manga with the total count of chapters. */
    class Chapters {
        @JsonProperty("edges")
        var edge: List<Edge>? = null

        @JsonProperty("totalCount")
        var totalCount: Long = 0
    }

    class Edge {
        @JsonProperty("node")
        var node: Node? = null
    }

    class Node {
        @JsonProperty("id")
        var id: Int = 0
    }

    class MangaCategories {
        @JsonProperty("nodes")
        var nodes: List<Category>? = null
    }

    /**
     * Get the total number of chapters for this manga. If the chapter count is not set, the count
     * will be retrieved from the [Chapters chapters] object if available. The default value is
     * 0.
     *
     * @return the total number of chapters for this manga
     */
    fun getChapterCountValue(): Int {
        if (this.chapterCount != 0) {
            return chapterCount
        }
        return chapters?.totalCount?.toInt() ?: 0
    }
}
