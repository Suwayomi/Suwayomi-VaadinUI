/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.card;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import lombok.Getter;

@Getter
@CssImport("./css/card.css")
public class Card extends Anchor {

  private final Paragraph textComponent;

  public Card(String title, String imageUrl) {
    setClassName("card");
    addClassName("shadow-m");
    addClassName("border");

    Image img = new Image(imageUrl, "Thumbnail");
    img.addClassName("card-img");

    Paragraph p = new Paragraph(title);
    p.addClassName("card-title");
    add(img, p);

    this.textComponent = p;
  }
}
