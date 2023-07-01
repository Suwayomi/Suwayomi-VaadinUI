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
