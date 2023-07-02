package online.hatsunemiku.tachideskvaadinui.component.items;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;

@CssImport("./css/components/items/blurry-item.css")
public class BlurryItem extends Div {

  public BlurryItem() {}

  protected void setContent(Component component) {
    Div blurryBackground = new Div();
    blurryBackground.setClassName("blurry-background");

    Div content = new Div();
    content.getStyle().set("z-index", "1");

    content.add(component);

    add(content, blurryBackground);
  }
}
