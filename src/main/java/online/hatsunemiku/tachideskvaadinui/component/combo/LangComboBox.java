package online.hatsunemiku.tachideskvaadinui.component.combo;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceLangFilterUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceLangUpdateEvent;

public class LangComboBox extends ComboBox<String> implements ComponentEventListener<SourceLangUpdateEvent> {

  public LangComboBox() {
    super("Language");

    addValueChangeListener(e -> {
      String newVal = e.getValue();

      if (newVal == null) {
        newVal = "";
      }

      var filterUpdateEvent = new SourceLangFilterUpdateEvent(this, newVal);
      ComponentUtil.fireEvent(UI.getCurrent(), filterUpdateEvent);
    });
  }

  @Override
  public void onComponentEvent(SourceLangUpdateEvent event) {
    String currentVal = getValue();

    setItems(event.getLanguages());

    if (currentVal != null && event.getLanguages().contains(currentVal)) {
      setValue(currentVal);
    }

    boolean langsExist = !event.getLanguages().isEmpty();

    setEnabled(langsExist);
  }
}
