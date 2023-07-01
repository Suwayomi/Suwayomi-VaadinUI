package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceFilterChangeEvent;
import online.hatsunemiku.tachideskvaadinui.component.scroller.source.SourceScroller;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("sources")
@CssImport("./css/views/sources.css")
public class SourcesView extends StandardLayout {

  public SourcesView(SourceService sources) {
    super("Sources");

    VerticalLayout content = new VerticalLayout();

    HorizontalLayout searches = new HorizontalLayout();

    TextField search = new TextField("Search by name");
    search.setPlaceholder("LHTranslation");
    search.addValueChangeListener(e -> {
      String filterText = e.getValue();
      if (filterText == null) {
        return;
      }

      SourceFilterChangeEvent event = new SourceFilterChangeEvent(search, filterText);
      fireEvent(event);
    });

    searches.add(search);

    SourceScroller scroller = new SourceScroller(sources);
    addListener(SourceFilterChangeEvent.class, scroller);

    content.add(searches, scroller);
    content.addClassName("sources-content");

    setContent(content);
  }
}
