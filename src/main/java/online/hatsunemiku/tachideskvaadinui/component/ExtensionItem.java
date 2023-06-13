package online.hatsunemiku.tachideskvaadinui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;

@NpmPackage(value = "vanilla-lazyload", version = "17.8.3")
@JavaScript("./js/lazyload.js")
@CssImport("./css/components/extension-item.css")
public class ExtensionItem extends BlurryItem {

  public ExtensionItem(Extension extension, Settings settings) {
    super();
    addClassName("extension-item");

    Div extensionData = new Div();
    extensionData.setClassName("extension-data");

    Div icon = new Div();
    icon.setClassName("extension-icon");

    String iconUrl = settings.getUrl() + extension.getIconUrl();
    Image image = new Image();

    image.setSrc(iconUrl);

    image.setClassName("extension-icon-image");
    image.addClassName("lazy");
    icon.add(image);

    Div name = new Div();
    name.setClassName("extension-name");
    name.setText(extension.getName());

    Div version = new Div();
    version.setClassName("extension-version");
    version.setText(extension.getVersionName());

    extensionData.add(icon, name, version);

    Button installBtn = new Button("Install");
    installBtn.setClassName("extension-install-btn");

    installBtn.addClickListener(event -> {
      System.out.println("Install " + extension.getName());
    });

    add(extensionData, installBtn);
  }

}
