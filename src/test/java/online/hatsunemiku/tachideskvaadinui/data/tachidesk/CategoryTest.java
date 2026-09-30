/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void testDefaultCategoryFallbackWhenIdIsZero() {
    Category category = new Category();
    assertEquals(0, category.getId());
    assertTrue(category.isDefaultCategory());
  }

  @Test
  void testNonDefaultCategoryWhenIdIsNotZero() {
    Category category = new Category(1, 1, "Reading", false, false);
    assertEquals(1, category.getId());
    assertFalse(category.isDefaultCategory());
  }

  @Test
  void testDefaultCategoryWithNonZeroIdForUserAccountsPreview() {
    // In upcoming user accounts preview, a user's default category may have id != 0
    Category category = new Category(42, 0, "Default", false, true);
    assertEquals(42, category.getId());
    assertTrue(category.isDefaultCategory());
  }

  @Test
  void testSetDefaultCategory() {
    Category category = new Category(1, 1, "Custom", false, false);
    assertFalse(category.isDefaultCategory());

    category.setDefaultCategory(true);
    assertTrue(category.isDefaultCategory());

    category.setDefaultCategory(false);
    assertFalse(category.isDefaultCategory());
  }
}
