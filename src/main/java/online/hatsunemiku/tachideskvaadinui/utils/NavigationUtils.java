package online.hatsunemiku.tachideskvaadinui.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;

@UtilityClass
public class NavigationUtils {

  public void navigateToReader(int mangaId, int chapterIndex, UI ui) {
    String mangaIdStr = Integer.toString(mangaId);
    String chapterIndexStr = Integer.toString(chapterIndex);

    RouteParam mangaIdParam = new RouteParam("mangaId", mangaIdStr);
    RouteParam chapterIndexParam = new RouteParam("chapterIndex", chapterIndexStr);

    RouteParameters params = new RouteParameters(mangaIdParam, chapterIndexParam);

    ui.navigate(ReadingView.class, params);
  }
}
