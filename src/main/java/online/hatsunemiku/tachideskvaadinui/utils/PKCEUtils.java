/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class PKCEUtils {

  private static final SecureRandom RANDOM = new SecureRandom();

  public static String generateCodeVerifier(int length) {
    byte[] verifier = new byte[length];
    RANDOM.nextBytes(verifier);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(verifier).substring(0, length);
  }
}
