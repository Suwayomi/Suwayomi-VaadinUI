package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.scroller.ExtensionScroller;
import online.hatsunemiku.tachideskvaadinui.services.ExtensionService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("extensions")
@CssImport("./css/views/extensions.css")
public class ExtensionsView extends StandardLayout {

  public ExtensionsView(ExtensionService extensionsService, SettingsService settingsService) {
    super("Extensions");
    setClassName("extensions-view");
    VerticalLayout content = new VerticalLayout();

    ExtensionScroller extensionsList = new ExtensionScroller(extensionsService, settingsService);

    TextField search = new TextField("Search");
    search.setPlaceholder("Search");
    search.setClearButtonVisible(true);

    search.addValueChangeListener(
        e -> {
          if (e.getValue().isEmpty()) {
            extensionsList.reset();
          }

          extensionsList.search(e.getValue());
        });

    content.add(search, extensionsList);

    setContent(content);
  }
}
