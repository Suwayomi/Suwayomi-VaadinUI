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
