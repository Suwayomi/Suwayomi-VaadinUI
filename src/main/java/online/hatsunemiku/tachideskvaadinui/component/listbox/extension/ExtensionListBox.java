package online.hatsunemiku.tachideskvaadinui.component.listbox.extension;

import com.vaadin.flow.component.listbox.ListBox;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;

public class ExtensionListBox extends ListBox<Extension> {

    public ExtensionListBox() {
      super();
      Settings settings = SerializationUtils.deseralizeSettings();
      setRenderer(new ExtensionRenderer(settings));
    }
}
