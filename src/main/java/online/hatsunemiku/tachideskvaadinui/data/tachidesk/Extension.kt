/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk

import com.fasterxml.jackson.annotation.JsonProperty

class Extension {
    @get:JsonProperty("isInstalled")
    @set:JsonProperty("isInstalled")
    var isInstalled: Boolean = false

    @get:JsonProperty("hasUpdate")
    @set:JsonProperty("hasUpdate")
    var isHasUpdate: Boolean = false
    
    var apkName: String? = null

    @get:JsonProperty("isNsfw")
    @set:JsonProperty("isNsfw")
    var isNsfw: Boolean = false

    var pkgName: String? = null
    var name: String? = null

    @get:JsonProperty("isObsolete")
    @set:JsonProperty("isObsolete")
    var isObsolete: Boolean = false

    var iconUrl: String? = null
    var versionName: String? = null
    var lang: String? = null
    var versionCode: Int = 0
}
