package online.hatsunemiku.tachideskvaadinui.component.dialog.category;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import online.hatsunemiku.tachideskvaadinui.component.dialog.category.events.CategoryCreationEvent;
import online.hatsunemiku.tachideskvaadinui.component.dialog.category.events.CategoryCreationListener;
import online.hatsunemiku.tachideskvaadinui.data.Category;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.CategoryUtils;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.springframework.web.client.RestTemplate;

public class CategoryDialog extends Dialog {

  private Binder<CategoryNameDTO> binder = new Binder<>();

  public CategoryDialog(RestTemplate client) {
    setHeaderTitle("Create Category");

    CategoryNameDTO categoryNameDTO = new CategoryNameDTO();

    TextField nameInput = new TextField();

    nameInput.setLabel("Name");
    nameInput.setPlaceholder("Type a name");
    nameInput.setRequired(true);

    binder.setBean(categoryNameDTO);
    binder.forField(nameInput)
        .withValidator(name -> name.length() > 0, "Name cannot be empty")
        .bind(CategoryNameDTO::getName, CategoryNameDTO::setName);

    Button cancelButton = new Button("Cancel");
    cancelButton.addClickListener(e -> close());

    Button createButton = new Button("Create");
    createButton.addClickListener(e -> createCategory(client, nameInput.getValue()));

    add(nameInput);

    getFooter().add(cancelButton, createButton);
  }

  private void createCategory(RestTemplate template, String name) {
    var status = binder.validate();
    if (status.hasErrors()) {
      return;
    }

    Settings settings = SerializationUtils.deseralizeSettings();
    boolean created = CategoryUtils.createCategory(template, settings, name);

    if (!created) {
      Notification notification = new Notification();
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setText("Failed to create category");
      notification.open();
    }

    List<Category> categories = CategoryUtils.getCategories(template, settings);

    Optional<Category> c = categories.stream()
        .filter(category -> category.getName().equals(name))
        .max(Comparator.comparingInt(Category::getId));

    if (c.isEmpty()) {
      UI.getCurrent().getPage().reload();
      return;
    }

    CategoryCreationEvent event = new CategoryCreationEvent(this, c.get());
    fireEvent(event);

    close();
  }

  public void addOnCategoryCreationListener(CategoryCreationListener listener) {
    addListener(CategoryCreationEvent.class, listener);
  }

}
