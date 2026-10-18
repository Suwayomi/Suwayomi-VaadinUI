/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.startup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SuwayomiStarterTest {

  @Test
  void testIsJavaInstalledAndSupported() {
    SuwayomiStarter starter = new SuwayomiStarter(null, null, null, null);
    assertTrue(starter.isJavaInstalledAndSupported());
  }
}
