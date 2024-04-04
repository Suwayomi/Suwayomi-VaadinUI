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
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.card.Card;

/**
 * TrackingLayout is a layout/base for displaying tracking information to import.
 */
@CssImport("./css/views/imports/importCommons.css")
public abstract class TrackingLayout extends StandardLayout {

  /**
   * Creates the base Structure for the layout.
   *
   * @param title the title of the view to display
   */
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

  public Div getContentSection(String title, List<? extends Card> content) {
    Div section = new Div();
    section.addClassName("import-content");

    Div titleSection = new Div();
    titleSection.addClassName("import-title-section");
    titleSection.setText(title);

    Div contentGrid = new Div();
    contentGrid.addClassName("import-content-grid");

    content.forEach(contentGrid::add);

    section.add(titleSection, contentGrid);

    return section;
  }
}
