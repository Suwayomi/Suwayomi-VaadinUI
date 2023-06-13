package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.scroller.ExtensionScroller;
import online.hatsunemiku.tachideskvaadinui.services.ExtensionService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("extensions")
public class ExtensionsView extends StandardLayout {

  private static final Logger logger = LoggerFactory.getLogger(ExtensionsView.class);
  private final ExtensionService extensionsService;

  public ExtensionsView(ExtensionService extensionsService) {
    super("Extensions");
    VerticalLayout content = new VerticalLayout();

    this.extensionsService = extensionsService;

    ExtensionScroller extensionsList = new ExtensionScroller(extensionsService);

    TextField search = new TextField("Search");
    search.setPlaceholder("Search");
    search.setClearButtonVisible(true);

    search.addValueChangeListener(e -> {

      if (e.getValue().isEmpty()) {
        extensionsList.reset();
      }

      extensionsList.search(e.getValue());
    });

    content.add(search, extensionsList);

    setContent(content);
  }

}
