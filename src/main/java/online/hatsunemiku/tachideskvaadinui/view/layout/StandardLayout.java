package online.hatsunemiku.tachideskvaadinui.view.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.time.LocalDate;
import org.jetbrains.annotations.NotNull;

@CssImport("css/common.css")
public class StandardLayout extends VerticalLayout {

  private H1 title;
  private HorizontalLayout navBar;
  private VerticalLayout content;

  public StandardLayout(String title) {
    setId("container");
    getNavBar(title);

    content = new VerticalLayout();
    content.setClassName("content");

    var footer = getFooter();

    this.add(navBar, content, footer);
  }

  private void getNavBar(String title) {
    this.title = new H1(title);

    navBar = new HorizontalLayout();
    navBar.add(this.title);

    navBar.setClassName("navbar");
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


}
