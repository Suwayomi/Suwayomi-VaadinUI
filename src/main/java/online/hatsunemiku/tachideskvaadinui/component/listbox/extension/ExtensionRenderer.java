package online.hatsunemiku.tachideskvaadinui.component.listbox.extension;

import com.vaadin.flow.data.renderer.ComponentRenderer;
import online.hatsunemiku.tachideskvaadinui.component.ExtensionItem;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;

public class ExtensionRenderer extends ComponentRenderer<ExtensionItem, Extension> {

  public ExtensionRenderer(Settings settings) {
    super(extension -> new ExtensionItem(extension, settings));
  }
}
