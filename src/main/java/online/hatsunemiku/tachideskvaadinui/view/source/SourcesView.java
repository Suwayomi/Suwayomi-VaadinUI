package online.hatsunemiku.tachideskvaadinui.view.source;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.combo.LangComboBox;
import online.hatsunemiku.tachideskvaadinui.component.events.source.SourceFilterUpdateEvent;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceScroller;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("sources")
@CssImport("./css/views/sources.css")
public class SourcesView extends StandardLayout {

  public SourcesView(SourceService sources, SettingsService settingsService) {
    super("Sources");

    VerticalLayout content = new VerticalLayout();

    HorizontalLayout filters = new HorizontalLayout();
    filters.addClassName("sources-filters");

    TextField nameFilter = new TextField("Search by name");
    nameFilter.setPlaceholder("LHTranslation");
    nameFilter.addValueChangeListener(
        e -> {
          String filterText = e.getValue();
          if (filterText == null) {
            return;
          }

          SourceFilterUpdateEvent event = new SourceFilterUpdateEvent(nameFilter, filterText);
          ComponentUtil.fireEvent(UI.getCurrent(), event);
        });

    LangComboBox langFilter = new LangComboBox();
    langFilter.addClassName("source-lang-filter");
    langFilter.setAllowCustomValue(false);

    filters.add(nameFilter, langFilter);

    SourceScroller scroller = new SourceScroller(sources, settingsService);
    scroller.addLangUpdateEventListener(langFilter);

    content.add(filters, scroller);
    content.addClassName("sources-content");

    setContent(content);
  }
}
