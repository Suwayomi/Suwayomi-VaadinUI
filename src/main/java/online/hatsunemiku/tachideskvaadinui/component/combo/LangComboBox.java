/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.combo;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import online.hatsunemiku.tachideskvaadinui.component.events.source.LanguageListChangeEvent;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceLangFilterUpdateEvent;

/**
 * This class represents a Combo box component with language selection. It includes the
 * functionality for updating the language list and firing events when the language selection
 * changes.
 *
 * @author aless2003
 * @version 1.1.0
 * @since 1.0.0
 */
public class LangComboBox extends ComboBox<String>
    implements ComponentEventListener<LanguageListChangeEvent> {

  private final List<Function<Void, Void>> langUpdateListeners = new ArrayList<>();

  /** Creates a new LangComboBox object with the default label "Language". */
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

    getListDataView()
        .addItemCountChangeListener(
            e -> {
              if (e.getItemCount() > 0) {
                langUpdateListeners.forEach(listener -> listener.apply(null));
              }
            });
  }

  @Override
  public void onComponentEvent(LanguageListChangeEvent event) {
    String currentVal = getValue();

    UI ui;

    if (getUI().isEmpty()) {
      if (UI.getCurrent() == null) {
        return;
      }

      ui = UI.getCurrent();
    } else {
      ui = getUI().get();
    }

    if (!ui.isAttached()) {
      return;
    }
    boolean langsExist = !event.getLanguages().isEmpty();

    ui.access(
        () -> {
          setItems(event.getLanguages());

          if (currentVal != null && event.getLanguages().contains(currentVal)) {
            setValue(currentVal);
          }

          setEnabled(langsExist);
        });
  }

  /**
   * Adds a listener to the list of listeners that will be called when the language list has been
   * updated.
   *
   * @param listener The listener to add to the list in the form of a {@link Function}.
   */
  public void addLangUpdateEventListener(Function<Void, Void> listener) {
    langUpdateListeners.add(listener);
  }
}
