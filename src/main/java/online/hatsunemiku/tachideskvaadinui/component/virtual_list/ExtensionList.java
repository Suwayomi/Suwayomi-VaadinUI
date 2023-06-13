package online.hatsunemiku.tachideskvaadinui.component.virtual_list;

import com.vaadin.flow.component.virtuallist.VirtualList;
import online.hatsunemiku.tachideskvaadinui.component.listbox.extension.ExtensionRenderer;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;

public class ExtensionList extends VirtualList<Extension> {

  public ExtensionList(Settings settings) {
    super();
    setRenderer(new ExtensionRenderer(settings));
  }

}
