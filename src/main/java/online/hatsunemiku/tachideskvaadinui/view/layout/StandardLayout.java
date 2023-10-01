/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.time.LocalDate;
import online.hatsunemiku.tachideskvaadinui.view.ExtensionsView;
import online.hatsunemiku.tachideskvaadinui.view.RootView;
import online.hatsunemiku.tachideskvaadinui.view.SearchView;
import online.hatsunemiku.tachideskvaadinui.view.SettingsView;
import online.hatsunemiku.tachideskvaadinui.view.source.SourcesView;
import org.jetbrains.annotations.NotNull;

@CssImport("./css/common.css")
public class StandardLayout extends VerticalLayout {

  private HorizontalLayout navBar;
  private final VerticalLayout content;
  private final Footer footer;

  public StandardLayout(String title) {
    setId("container");
    getNavBar(title);

    content = new VerticalLayout();
    content.setClassName("content");

    var footer = getFooter();
    this.footer = footer;

    this.add(navBar, content, footer);
  }

  private void getNavBar(String title) {
    H1 title1 = new H1(title);

    navBar = new HorizontalLayout();
    navBar.setClassName("navbar");
    navBar.add(title1);

    Div btnContainer = new Div();
    btnContainer.setClassName("btn-container");

    addRootBtn(btnContainer);
    addExtensionsBtn(btnContainer);
    addSourcesBtn(btnContainer);
    addSearchBtn(btnContainer);
    addSpacer(btnContainer);
    addSettingsBtn(btnContainer);

    navBar.add(btnContainer);
  }

  public void addSettingsBtn(Div btnContainer) {
    if (this instanceof SettingsView) {
      return;
    }

    Button settingsButton = new Button(VaadinIcon.COG.create());
    settingsButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SettingsView.class)));

    addBtn(btnContainer, settingsButton);
  }

  private void addSpacer(Div btnContainer) {
    Div spacer = new Div();
    spacer.setClassName("spacer");
    btnContainer.add(spacer);
  }

  private void addSearchBtn(Div btnContainer) {

    if (this instanceof SearchView) {
      return;
    }

    Button searchButton = new Button("Search", VaadinIcon.SEARCH.create());

    searchButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SearchView.class)));

    addBtn(btnContainer, searchButton);
  }

  private void addSourcesBtn(Div btnContainer) {

    if (this instanceof SourcesView) {
      return;
    }

    Button sourcesButton = new Button("Sources", VaadinIcon.GLOBE.create());
    sourcesButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SourcesView.class)));

    addBtn(btnContainer, sourcesButton);
  }

  private void addRootBtn(Div btnContainer) {

    if (this instanceof RootView) {
      return;
    }

    Button rootButton = new Button("Library", VaadinIcon.BOOK.create());
    rootButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(RootView.class)));

    addBtn(btnContainer, rootButton);
  }

  private void addExtensionsBtn(Div btnContainer) {

    if (this instanceof ExtensionsView) {
      return;
    }

    Button extensionsButton = new Button("Extensions", VaadinIcon.PUZZLE_PIECE.create());
    extensionsButton.addClickListener(
        e -> getUI().ifPresent(ui -> ui.navigate(ExtensionsView.class)));

    addBtn(btnContainer, extensionsButton);
  }

  private void addBtn(Div btnContainer, Button btn) {
    btn.addClassName("nav-btn");
    btnContainer.add(btn);
  }

  @NotNull
  private Footer getFooter() {
    Footer footer = new Footer();

    int copyrightYear = LocalDate.now().getYear();
    String copyright = "© %d Alessandro Schwaiger".formatted(copyrightYear);

    footer.add(copyright);
    return footer;
  }

  protected void setContent(Component content) {
    this.content.removeAll();
    this.content.add(content);
  }

  protected void fullScreen() {
    this.content.setClassName("content-fullscreen");
    this.navBar.setVisible(false);
    this.footer.setVisible(false);
    addClassName("fullscreen");
  }

  protected void fullScreenNoHide() {
    this.content.setClassName("content-fullscreen");
    addClassName("fullscreen");
  }

  /**
   * Sets the UI to windowed mode by updating the class names and visibility of different
   * components. This method is only supposed to be called when {@link #fullScreen()} was called
   * before.
   */
  protected void windowed() {
    this.content.setClassName("content");
    this.navBar.setVisible(true);
    this.footer.setVisible(true);
    removeClassName("fullscreen");
  }
}
