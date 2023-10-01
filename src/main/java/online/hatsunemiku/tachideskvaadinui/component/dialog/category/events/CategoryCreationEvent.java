/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.category.events;

import com.vaadin.flow.component.ComponentEvent;
import online.hatsunemiku.tachideskvaadinui.component.dialog.category.CategoryDialog;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;

public class CategoryCreationEvent extends ComponentEvent<CategoryDialog> {

  private final Category category;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param category The category that was created
   */
  public CategoryCreationEvent(CategoryDialog source, Category category) {
    super(source, true);
    this.category = category;
  }

  public Category getCategory() {
    return category;
  }
}
