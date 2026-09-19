/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk

import java.time.Instant

class TrackRecord {
    var id: Int = 0
    var libraryId: Long = 0
    var mangaId: Int = 0
    var remoteId: Long = 0
    var trackerId: Int = 0
    var remoteUrl: String? = null
    var title: String? = null
    var lastChapterRead: Float = 0.0f
    var totalChapters: Int = 0
    var displayScore: String? = null
    var finishDate: Instant? = null
    var startDate: Instant? = null
    var score: Float = 0.0f
    var status: Int = 0
}
