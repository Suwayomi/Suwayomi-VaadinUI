package online.hatsunemiku.tachideskvaadinui.component.items;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;

@CssImport("./css/components/items/source-item.css")
public class SourceItem extends BlurryItem {

  public SourceItem(Source source, Settings settings) {
    Div container = new Div();
    container.setClassName("source-item");

    Div title = new Div();
    title.setText(source.getDisplayName());
    title.setClassName("source-item-title");

    Div icon = new Div();
    icon.setClassName("source-item-icon");

    Image iconImg = new Image();
    iconImg.setClassName("source-item-icon-img");
    iconImg.setSrc(settings.getUrl() + source.getIconUrl());

    icon.add(iconImg);

    Div buttons = new Div();
    buttons.addClassName("source-item-buttons");

    Button exploreBtn = new Button("Explore");
    exploreBtn.addClassName("source-item-explore-btn");
    exploreBtn.addClickListener(
        e -> {
          var possibleUi = getUI();
          if (possibleUi.isPresent()) {
            var ui = possibleUi.get();
            ui.navigate("source/explore/" + source.getId());
          }
        });

    buttons.add(exploreBtn);

    Div infos = new Div();
    infos.add(icon, title);
    infos.setClassName("source-item-infos");

    container.add(infos, buttons);

    add(container);
  }
}
