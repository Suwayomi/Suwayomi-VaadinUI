package online.hatsunemiku.tachideskvaadinui.component.scroller;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.ExtensionItem;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Extension;
import online.hatsunemiku.tachideskvaadinui.services.ExtensionService;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import org.vaadin.firitin.components.html.VDiv;
import org.vaadin.firitin.components.orderedlayout.VScroller;

@CssImport("./css/components/extension-scroller.css")
public class ExtensionScroller extends VScroller {

  public static final int LIST_SIZE = 15;
  private final ExtensionService service;
  private int page = 0;
  private int maxPage;
  private final Div content;
  private String search;

  public ExtensionScroller(ExtensionService service) {
    super();
    this.service = service;
    this.content = new VDiv();

    setClassName("extension-scroller");

    Settings settings = SerializationUtils.deseralizeSettings();

    List<Extension> extensions = service.getExtensions();

    maxPage = extensions.size() / LIST_SIZE;

    addExtensions(extensions, settings);
    addScrollToEndListener(e -> {
      if (page < maxPage) {
        page++;
        addExtensions(extensions, settings);
      }
    });
  }


  private void addExtensions(List<Extension> extensions, Settings settings) {

    extensions = filterExtensions(search, extensions);
    maxPage = extensions.size() / LIST_SIZE;

    int startIndex = page * LIST_SIZE;

    if (startIndex > extensions.size()) {
      return;
    }

    int endIndex = startIndex + LIST_SIZE;

    if (endIndex > extensions.size()) {
      endIndex = extensions.size();
    }

    List<Extension> subList = extensions.subList(startIndex, endIndex);

    content.setClassName("extension-scroller-content");

    for (Extension extension : subList) {
      content.add(new ExtensionItem(extension, settings));
    }

    setContent(content);
  }

  public void search(String value) {
    this.search = value;
    content.removeAll();
    page = 0;
    var extensions = service.getExtensions();
    var settings = SerializationUtils.deseralizeSettings();
    addExtensions(extensions, settings);
  }

  public void reset() {
    content.removeAll();
    page = 0;
    this.search = null;
    var extensions = service.getExtensions();
    var settings = SerializationUtils.deseralizeSettings();
    addExtensions(extensions, settings);
  }

  private List<Extension> filterExtensions(String search, List<Extension> extensions) {
    return extensions.stream().filter(extension -> {
      if (search == null) {
        return true;
      }

      String name = extension.getName().toLowerCase();
      String searchLower = search.toLowerCase();

      return name.contains(searchLower);
    }).toList();
  }
}
