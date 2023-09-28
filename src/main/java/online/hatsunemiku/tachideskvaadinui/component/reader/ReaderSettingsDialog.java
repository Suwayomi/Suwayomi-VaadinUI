package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.Binder;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.ReaderSettingsChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderDirection;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;

@CssImport("./css/components/reader/reader-settings-dialog.css")
public class ReaderSettingsDialog extends Dialog {

  public ReaderSettingsDialog(Settings settings, int mangaId) {
    addClassName("reader-settings-dialog");

    var readerSettings = settings.getReaderSettings(mangaId);

    Binder<ReaderSettings> binder = new Binder<>(ReaderSettings.class);

    ComboBox<ReaderDirection> directionComboBox = new ComboBox<>("Direction");
    directionComboBox.setItems(ReaderDirection.values());
    directionComboBox.setAllowCustomValue(false);

    binder.forField(directionComboBox)
        .bind(ReaderSettings::getDirection, ReaderSettings::setDirection);

    binder.readBean(readerSettings);

    Div buttonContainer = new Div();
    buttonContainer.addClassName("button-container");

    Button saveBtn = new Button("Save as Default", e -> {
      var defaultSettings = settings.getDefaultReaderSettings();
      if (!binder.writeBeanIfValid(defaultSettings)) {
        Notification notification = new Notification("Invalid inputs", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.open();
        return;
      }

      Notification notification = new Notification("Saved Settings", 3000);
      notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      notification.setPosition(Notification.Position.MIDDLE);
      notification.open();
      close();

      UI ui = UI.getCurrent();

      if (!settings.hasMangaReaderSettings(mangaId)) {
        ComponentUtil.fireEvent(ui, new ReaderSettingsChangeEvent(this, false, defaultSettings));
      }
    });

    Button saveForMangaBtn = new Button("Save for this manga", e -> {
      ReaderSettings newSettings = new ReaderSettings();
      if (!binder.writeBeanIfValid(newSettings)) {
        Notification notification = new Notification("Invalid inputs", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.open();
        return;
      }

      settings.addMangaReaderSettings(mangaId, newSettings);

      Notification notification = new Notification("Saved Settings", 3000);
      notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      notification.setPosition(Notification.Position.MIDDLE);
      notification.open();
      close();

      UI ui = UI.getCurrent();
      ComponentUtil.fireEvent(ui, new ReaderSettingsChangeEvent(this, false, newSettings));
    });

    Button cancelBtn = new Button("Cancel", e -> close());

    buttonContainer.add(saveBtn, saveForMangaBtn, cancelBtn);

    add(directionComboBox, buttonContainer);
  }
}
