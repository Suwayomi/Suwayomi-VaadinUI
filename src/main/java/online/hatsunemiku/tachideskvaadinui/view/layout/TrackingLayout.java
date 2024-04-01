/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.layout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
@CssImport("./css/views/imports/importCommons.css")
public abstract class TrackingLayout extends StandardLayout {

  public TrackingLayout(String title) {
    super(title);

    addClassName("tracking-layout");
  }

  protected void init() {
    if (!hasToken()) {
      Div content = new Div();
      content.addClassName("button-container");

      Button authBtn = new Button("Authenticate");
      authBtn.addClickListener(e -> authenticate());

      content.add(authBtn);
      setContent(content);
      return;
    }

    var reading = getReadingSection();
    var planToRead = getPlanToReadSection();
    var completed = getCompletedSection();
    var onHold = getOnHoldSection();
    var dropped = getDroppedSection();

    VerticalLayout content = new VerticalLayout();

    content.add(reading, planToRead, completed, onHold, dropped);

    setContent(content);
  }

  public abstract boolean hasToken();

  public abstract void authenticate();

  public abstract Div getReadingSection();

  public abstract Div getPlanToReadSection();

  public abstract Div getCompletedSection();

  public abstract Div getOnHoldSection();

  public abstract Div getDroppedSection();

}
