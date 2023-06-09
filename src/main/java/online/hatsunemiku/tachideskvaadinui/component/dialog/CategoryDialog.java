package online.hatsunemiku.tachideskvaadinui.component.dialog;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.CategoryUtils;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.springframework.web.client.RestTemplate;

public class CategoryDialog extends Dialog {

  public CategoryDialog(RestTemplate client) {
    setHeaderTitle("Create Category");

    TextField nameInput = new TextField();

    nameInput.setLabel("Name");
    nameInput.setPlaceholder("Type a name");
    nameInput.setRequired(true);

    Button cancelButton = new Button("Cancel");
    cancelButton.addClickListener(e -> close());

    Button createButton = new Button("Create");
    createButton.addClickListener(e -> createCategory(client, nameInput.getValue()));

    add(nameInput);

    getFooter().add(cancelButton, createButton);
  }

  private void createCategory(RestTemplate template, String name) {
    Settings settings = SerializationUtils.deseralizeSettings();
    boolean created = CategoryUtils.createCategory(template, settings, name);

    if (!created) {
      Notification notification = new Notification();
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setText("Failed to create category");
      notification.open();
    }

    close();

    UI.getCurrent().getPage().reload();
  }

}
