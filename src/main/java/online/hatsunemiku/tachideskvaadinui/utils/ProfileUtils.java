/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

public class ProfileUtils {
  public static boolean isDev(Environment env) {
    Profiles profiles = Profiles.of("dev");
    return env.acceptsProfiles(profiles);
  }

  public static boolean isDev() {
    String profiles = System.getProperty("spring.profiles.active");
    if (profiles == null) {
      profiles = System.getenv("SPRING_PROFILES_ACTIVE");
    }
    return profiles != null && Arrays.asList(profiles.split(",")).contains("dev");
  }
}
