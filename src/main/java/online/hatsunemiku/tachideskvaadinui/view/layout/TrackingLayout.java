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
  protected TrackingLayout(String title) {
    super(title);

    addClassName("tracking-layout");
  }

  /**
   * Initializes the tracking layout based on the availability of a token.
   * If a token is available, it creates and adds the necessary sections to the layout.
   * If a token is not available, it adds an authentication button to the layout.
   */
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

    //TODO style auth button

    var reading = getReadingSection();
    var planToRead = getPlanToReadSection();
    var completed = getCompletedSection();
    var onHold = getOnHoldSection();
    var dropped = getDroppedSection();

    VerticalLayout content = new VerticalLayout();

    content.add(reading, planToRead, completed, onHold, dropped);

    setContent(content);
  }

  /**
   * Checks if there's a token available for the Tracking Service. This method should be implemented
   * by subclasses.
   *
   * @return {@code true} if the subclass has a token, {@code false} otherwise
   */
  public abstract boolean hasToken();

  /**
   * Authenticates the user with the Tracking Service. This method should be implemented by
   * subclasses to perform the necessary authentication logic.
   */
  public abstract void authenticate();

  /**
   * Returns the `Reading` manga section for the tracking layout. This method should be implemented
   * by subclasses.
   *
   * @return the `Reading` section as a Div element
   */

  public abstract Div getReadingSection();

  /**
   * Returns the `Plan to Read` manga section for the tracking layout. This method should be
   * implemented by subclasses.
   *
   * @return the `Plan to Read` section as a Div element
   */

  public abstract Div getPlanToReadSection();

  /**
   * Returns the `Completed` manga section for the tracking layout. This method should be
   * implemented by subclasses.
   *
   * @return the `Completed` section as a Div element
   */
  public abstract Div getCompletedSection();

  /**
   * Returns the `On Hold` manga section for the tracking layout. This method should be implemented
   * by subclasses.
   *
   * @return the `On Hold` section as a Div element
   */
  public abstract Div getOnHoldSection();

  /**
   * Returns the `Dropped` manga section for the tracking layout. This method should be implemented
   * by subclasses.
   *
   * @return the `Dropped` section as a Div element
   */
  public abstract Div getDroppedSection();

  /**
   * Generates a content section containing a title and a list of cards.
   *
   * @param title   the title of the content section
   * @param content the list of cards to be added to the content section
   * @return a Div element representing the content section
   */
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
