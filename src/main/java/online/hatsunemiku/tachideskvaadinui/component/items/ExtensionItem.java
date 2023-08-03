package online.hatsunemiku.tachideskvaadinui.component.items;

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
import org.jetbrains.annotations.NotNull;

@NpmPackage(value = "vanilla-lazyload", version = "17.8.3")
@JavaScript("./js/lazyload.js")
@CssImport("./css/components/extension-item.css")
public class ExtensionItem extends BlurryItem {

  public ExtensionItem(Extension extension, Settings settings, ExtensionService service) {
    super();
    addClassName("extension-item");

    Div extensionData = getExtensionStructure(extension, settings);

    Div buttons = new Div();
    buttons.setClassName("extension-buttons");

    Button installBtn = new Button("Install");
    Button uninstallBtn = new Button("Uninstall");

    configureInstallBtn(extension, service, uninstallBtn, installBtn);
    configureUninstallBtn(extension, service, installBtn, uninstallBtn);

    buttons.add(uninstallBtn);
    buttons.add(installBtn);

    if (extension.isInstalled()) {
      setBtnInstalled(installBtn, uninstallBtn);
    } else {
      setBtnUninstalled(installBtn, uninstallBtn);
    }

    add(extensionData, buttons);
  }

  @NotNull
  private static Div getExtensionStructure(Extension extension, Settings settings) {
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
    return extensionData;
  }

  private void configureUninstallBtn(Extension extension, ExtensionService service, Button installBtn, Button uninstallBtn) {
    uninstallBtn.setClassName("extension-uninstall-btn");

    uninstallBtn.addClickListener(
        e -> {
          uninstallBtn.setEnabled(false);
          var status = service.uninstallExtension(extension.getPkgName());
          uninstallBtn.setEnabled(true);

          Notification notification = new Notification();

          if (status.is2xxSuccessful()) {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setText("Extension uninstalled successfully");
          } else if (status.is3xxRedirection()) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Extension exists, but couldn't be uninstalled");
          } else if (status.is4xxClientError()) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Extension doesn't exist");
          } else {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Extension couldn't be uninstalled");
          }

          notification.setDuration(3000);
          notification.open();
          setBtnUninstalled(installBtn, uninstallBtn);
        });
  }

  private void configureInstallBtn(Extension extension, ExtensionService service, Button uninstallBtn, Button installBtn) {
    installBtn.setClassName("extension-install-btn");

    updateStatus(extension, installBtn, uninstallBtn);

    installBtn.addClickListener(
        event -> {
          installBtn.setEnabled(false);

          if (extension.isObsolete()) {
            service.uninstallExtension(extension.getPkgName());
            updateStatus(extension, installBtn, uninstallBtn);
            return;
          }

          if (extension.isHasUpdate()) {
            service.updateExtension(extension.getPkgName());
            updateStatus(extension, installBtn, uninstallBtn);
            installBtn.setEnabled(true);
            return;
          }

          var status = service.installExtension(extension.getPkgName());
          installBtn.setEnabled(true);
          updateStatus(extension, installBtn, uninstallBtn);
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
  }

  private void updateStatus(Extension extension, Button installBtn, Button uninstallBtn) {
    if (!extension.isInstalled()) {
      return;
    }

    if (extension.isObsolete()) {
      setBtnObsolete(installBtn, uninstallBtn);
    } else if (extension.isHasUpdate()) {
      setBtnUpdate(installBtn, uninstallBtn);
    } else {
      setBtnInstalled(installBtn, uninstallBtn);
    }
  }

  private void setBtnInstalled(Button installBtn, Button uninstallBtn) {
    installBtn.setEnabled(false);
    installBtn.setText("Installed");
    installBtn.addClassName("extension-installed-btn");
    uninstallBtn.setVisible(true);
  }

  private void setBtnUninstalled(Button installBtn, Button uninstallBtn) {
    installBtn.setEnabled(true);
    installBtn.setText("Install");
    installBtn.removeClassName("extension-installed-btn");
    uninstallBtn.setVisible(false);
  }

  private void setBtnObsolete(Button installBtn, Button uninstallBtn) {
    installBtn.setEnabled(true);
    installBtn.setText("Obsolete");
    installBtn.removeClassName("extension-installed-btn");
    uninstallBtn.setVisible(true);
  }

  private void setBtnUpdate(Button installBtn, Button uninstallBtn) {
    installBtn.setEnabled(true);
    installBtn.setText("Update");
    installBtn.removeClassName("extension-installed-btn");
    uninstallBtn.setVisible(true);
  }
}
