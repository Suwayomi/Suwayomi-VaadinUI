package online.hatsunemiku.tachideskvaadinui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.services.ExtensionService;

@NpmPackage(value = "vanilla-lazyload", version = "17.8.3")
@JavaScript("./js/lazyload.js")
@CssImport("./css/components/extension-item.css")
public class ExtensionItem extends BlurryItem {

  public ExtensionItem(Extension extension, Settings settings, ExtensionService service) {
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

    updateStatus(extension, installBtn);

    installBtn.addClickListener(event -> {
      installBtn.setEnabled(false);
      var status = service.installExtension(extension.getPkgName());
      installBtn.setEnabled(true);
      updateStatus(extension, installBtn);
      Notification notification = new Notification();

      if (status.is2xxSuccessful()) {
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setText("Extension installed successfully");
      } else if (status.is3xxRedirection()) {
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setText("Extension exists, but couldn't be installed");
      } else {
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setText("Extension couldn't be installed");
      }

      notification.setDuration(3000);
      notification.open();
    });

    add(extensionData, installBtn);
  }

  private void updateStatus(Extension extension, Button installBtn) {
    if (extension.isInstalled()) {
      installBtn.setEnabled(false);
      installBtn.setText("Installed");
    }
  }

}
