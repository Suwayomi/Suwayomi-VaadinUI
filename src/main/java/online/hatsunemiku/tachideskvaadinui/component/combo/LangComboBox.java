package online.hatsunemiku.tachideskvaadinui.component.combo;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import online.hatsunemiku.tachideskvaadinui.component.events.source.LanguageListChangeEvent;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceLangFilterUpdateEvent;

public class LangComboBox extends ComboBox<String>
    implements ComponentEventListener<LanguageListChangeEvent> {

  public LangComboBox() {
    super("Language");

    addValueChangeListener(
        e -> {
          String newVal = e.getValue();

          if (newVal == null) {
            newVal = "";
          }

          var filterUpdateEvent = new SourceLangFilterUpdateEvent(this, newVal);
          ComponentUtil.fireEvent(UI.getCurrent(), filterUpdateEvent);
        });
  }

  @Override
  public void onComponentEvent(LanguageListChangeEvent event) {
    String currentVal = getValue();

    if (getUI().isEmpty()) {
      return;
    }

    if (!getUI().get().isAttached()) {
      return;
    }
    boolean langsExist = !event.getLanguages().isEmpty();

    getUI()
        .get()
        .access(
            () -> {
              setItems(event.getLanguages());

              if (currentVal != null && event.getLanguages().contains(currentVal)) {
                setValue(currentVal);
              }

              setEnabled(langsExist);
            });
  }
}
