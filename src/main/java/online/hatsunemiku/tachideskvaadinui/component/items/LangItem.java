/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.items;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;

@CssImport("./css/components/items/lang-item.css")
public class LangItem extends BlurryItem {

  public LangItem(String lang) {
    Div container = new Div();
    container.setClassName("lang-item");

    Div title = new Div();
    title.setText(lang);
    title.setClassName("lang-item-title");

    container.add(title);

    setContent(container);
  }
}
