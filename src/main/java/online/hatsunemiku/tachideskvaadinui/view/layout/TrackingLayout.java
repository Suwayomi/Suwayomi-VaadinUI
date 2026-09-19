/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.layout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.card.Card;

/** TrackingLayout is a layout/base for displaying tracking information to import. */
@Slf4j
@CssImport("./css/views/imports/importCommons.css")
public abstract class TrackingLayout extends StandardLayout {

  /**
   * Creates the base Structure for the layout.
   *
   * @param title the title of the view to display
   */
  protected TrackingLayout(String title) {
    super(title);

    addClassNames("tracking-layout", "library-screen");
  }

  /**
   * Initializes the tracking layout based on the availability of a token. If a token is available,
   * it creates and adds the necessary sections to the layout. If a token is not available, it adds
   * an authentication button to the layout.
   */
  protected void init() {
    if (!hasToken()) {
      Div content = new Div();
      content.addClassName("import-button-container");

      Button authBtn = new Button("Authenticate");
      authBtn.addClickListener(e -> authenticate());

      content.add(authBtn);
      setContent(content);
      return;
    }

    Div reading;
    Div planToRead;
    Div completed;
    Div onHold;
    Div dropped;
    try {
      reading = getReadingSection();
      planToRead = getPlanToReadSection();
      completed = getCompletedSection();
      onHold = getOnHoldSection();
      dropped = getDroppedSection();
    } catch (Exception e) {
      log.error("Failed to init tracking sections", e);
      throw new RuntimeException(e);
    }

    Div content = new Div();
    content.addClassName("import-sections-container");

    Div hero = new Div();
    hero.addClassName("import-hero");
    Div heroLeft = new Div();
    heroLeft.addClassName("import-hero-left");
    H2 heroTitle = new H2(getTitle());
    Span heroSubtitle = new Span("Import and sync your manga list from external trackers.");
    heroSubtitle.addClassName("import-hero-subtitle");
    heroLeft.add(heroTitle, heroSubtitle);
    hero.add(heroLeft);

    content.add(hero, reading, planToRead, completed, onHold, dropped);

    setContent(content);
  }

  private String getTitle() {
    return switch (getClass().getSimpleName()) {
      case "AniListView" -> "AniList";
      case "MALView" -> "MyAnimeList";
      default -> "Tracker";
    };
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
   * @param title the title of the content section
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
