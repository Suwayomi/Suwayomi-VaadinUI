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
import online.hatsunemiku.tachideskvaadinui.view.RootView;
import org.jetbrains.annotations.NotNull;

@CssImport("css/common.css")
public class StandardLayout extends VerticalLayout {

  private H1 title;
  private HorizontalLayout navBar;
  private VerticalLayout content;
  private Footer footer;

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
    this.title = new H1(title);

    navBar = new HorizontalLayout();
    navBar.setClassName("navbar");
    navBar.add(this.title);

    Div btnContainer = new Div();
    btnContainer.setClassName("btn-container");

    if (!(this instanceof RootView)) {
      addRootBtn(btnContainer);
    }

    navBar.add(btnContainer);
  }

  private void addRootBtn(Div btnContainer) {
    Button rootButton = new Button("Library", VaadinIcon.BOOK.create());
    rootButton.addClickListener(e -> {
      getUI().ifPresent(ui -> ui.navigate(RootView.class));
    });

    addBtn(btnContainer, rootButton);
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
    this.content.add(content);
  }

  protected void fullScreen() {
    this.content.setClassName("content-fullscreen");
    this.navBar.setVisible(false);
    this.footer.setVisible(false);
    addClassName("fullscreen");
  }

  protected void windowed() {
    this.content.setClassName("content");
    this.navBar.setVisible(true);
    this.footer.setVisible(true);
    removeClassName("fullscreen");
  }


}
