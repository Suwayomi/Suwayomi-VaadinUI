/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import lombok.Data;

@Data
public class Extension {
  private boolean installed;
  private boolean hasUpdate;
  private String apkName;
  private boolean isNsfw;
  private String pkgName;
  private String name;
  private boolean obsolete;
  private String iconUrl;
  private String versionName;
  private String lang;
  private int versionCode;
}
