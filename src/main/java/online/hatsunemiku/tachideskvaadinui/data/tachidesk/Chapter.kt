/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a chapter object from the Suwayomi API.
 *
 * @since 0.9.0
 * @version 1.12.0
 */
class Chapter : Comparable<Chapter> {

    @JsonProperty("id")
    var id: Int = 0

    @JsonProperty("name")
    var name: String? = null

    @JsonProperty("chapterNumber")
    var chapterNumber: Float = 0.0f

    @JsonProperty("isDownloaded")
    var isDownloaded: Boolean = false

    @JsonProperty("isRead")
    var isRead: Boolean = false

    @JsonProperty("mangaId")
    var mangaId: Int = 0

    @JsonProperty("url")
    var url: String? = null

    @JsonProperty("pageCount")
    var pageCount: Int = 0

    @JsonProperty("scanlator")
    var scanlator: String? = null

    @JsonProperty("sourceOrder")
    var sourceOrder: Int = 0

    @JsonProperty("readAt")
    var readAt: Int = 0

    @JsonProperty("lastPageRead")
    var lastChapterRead: Int = 0

    @JsonProperty("dateFetch")
    var dateFetch: Long = 0

    @JsonProperty("dateUpload")
    var dateUpload: Long = 0

    var manga: Manga? = null

    override fun compareTo(other: Chapter): Int {
        return chapterNumber.compareTo(other.chapterNumber)
    }

    fun withDownloaded(downloaded: Boolean): Chapter {
        val chapter = Chapter()
        chapter.id = id
        chapter.name = name
        chapter.chapterNumber = chapterNumber
        chapter.isDownloaded = downloaded
        chapter.isRead = isRead
        chapter.mangaId = mangaId
        chapter.url = url
        chapter.pageCount = pageCount
        chapter.scanlator = scanlator
        chapter.sourceOrder = sourceOrder
        chapter.readAt = readAt
        chapter.lastChapterRead = lastChapterRead
        chapter.dateFetch = dateFetch
        chapter.dateUpload = dateUpload
        chapter.manga = manga
        return chapter
    }

    fun getUploadDate(): Long = dateUpload
}
