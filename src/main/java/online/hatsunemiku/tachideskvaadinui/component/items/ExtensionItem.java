/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.items;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.services.ExtensionService;
import org.jetbrains.annotations.NotNull;

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

  private void configureUninstallBtn(
      Extension extension, ExtensionService service, Button installBtn, Button uninstallBtn) {
    uninstallBtn.setClassName("extension-uninstall-btn");

    uninstallBtn.addClickListener(
        e -> {
          uninstallBtn.setEnabled(false);
          var success = service.uninstallExtension(extension.getPkgName());
          uninstallBtn.setEnabled(true);

          Notification notification = new Notification();

          if (!success) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Extension couldn't be uninstalled");
          } else {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setText("Extension uninstalled successfully");
            extension.setInstalled(false);
          }

          notification.setDuration(3000);
          notification.open();
          setBtnUninstalled(installBtn, uninstallBtn);
        });
  }

  private void configureInstallBtn(
      Extension extension, ExtensionService service, Button uninstallBtn, Button installBtn) {
    installBtn.setClassName("extension-install-btn");

    updateStatus(extension, installBtn, uninstallBtn);

    installBtn.addClickListener(
        event -> {
          installBtn.setEnabled(false);

          if (extension.isObsolete()) {
            service.uninstallExtension(extension.getPkgName());
            extension.setInstalled(false);
            updateStatus(extension, installBtn, uninstallBtn);
            return;
          }

          if (extension.isHasUpdate()) {
            service.updateExtension(extension.getPkgName());
            extension.setHasUpdate(false);
            updateStatus(extension, installBtn, uninstallBtn);
            return;
          }

          var success = service.installExtension(extension.getPkgName());
          installBtn.setEnabled(true);
          Notification notification = new Notification();

          if (!success) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setText("Extension couldn't be installed");
          } else {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setText("Extension installed successfully");
            extension.setInstalled(true);
          }

          notification.setDuration(3000);
          notification.open();

          updateStatus(extension, installBtn, uninstallBtn);
        });
  }

  private void updateStatus(Extension extension, Button installBtn, Button uninstallBtn) {
    if (!extension.isInstalled()) {
      setBtnUninstalled(installBtn, uninstallBtn);
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
