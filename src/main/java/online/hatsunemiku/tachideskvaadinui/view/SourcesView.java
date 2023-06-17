package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.router.Route;
import online.hatsunemiku.tachideskvaadinui.component.scroller.SourceScroller;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("sources")
public class SourcesView extends StandardLayout {

  private final SourceService sources;

  public SourcesView(SourceService sources) {
    super("Sources");
    this.sources = sources;

    SourceScroller scroller = new SourceScroller(sources);

    setContent(scroller);
  }
}
