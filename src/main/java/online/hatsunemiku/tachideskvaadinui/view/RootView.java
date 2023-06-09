package online.hatsunemiku.tachideskvaadinui.view;


import static org.springframework.http.HttpMethod.GET;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.component.dialog.CategoryDialog;
import online.hatsunemiku.tachideskvaadinui.data.Category;
import online.hatsunemiku.tachideskvaadinui.data.Manga;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.CategoryUtils;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

@Route("/")
@CssImport("css/root.css")

public class RootView extends StandardLayout {

  private final RestTemplate client;

  public RootView(RestTemplate client) {
    super("Library");

    this.client = client;
    Settings settings = SerializationUtils.deseralizeSettings();

    var categories = getCategories(settings);

    TabSheet tabs = new TabSheet();
    tabs.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
    addCategoryTabs(categories, tabs, settings);

    Button createButton = new Button(VaadinIcon.PLUS.create());
    createButton.addClickListener(e -> {
      CategoryDialog dialog = new CategoryDialog(client);

      dialog.addOpenedChangeListener(event -> {
        if (!event.isOpened()) {
          removeClassName("blur");
        } else {
          addClassName("blur");
        }
      });


      dialog.open();
    });

    tabs.setSuffixComponent(createButton);

    setContent(tabs);
  }

  private List<Category> getCategories(Settings settings) {
    String categoryEndpoint = settings.getUrl() + "/api/v1/category";

    ParameterizedTypeReference<List<Category>> typeRef = new ParameterizedTypeReference<>() {};

    List<Category> list = client.exchange(categoryEndpoint, GET, null, typeRef).getBody();

    if (list == null) {
      return new ArrayList<>();
    }

    return list;
  }

  private List<Manga> getManga(Category category, Settings settings) {
    String template = "%s/api/v1/category/%d";
    String categoryMangaEndpoint = String.format(template, settings.getUrl(), category.getId());

    ParameterizedTypeReference<List<Manga>> typeRef = new ParameterizedTypeReference<>() {};
    List<Manga> mangaList = client.exchange(categoryMangaEndpoint, GET, null, typeRef).getBody();

    if (mangaList == null) {
      return new ArrayList<>();
    }

    return mangaList;
  }

  private void addCategoryTabs(List<Category> categories, TabSheet tabs, Settings settings) {
    for (Category c : categories) {
      Tab tab = new Tab(c.getName());
      List<Manga> manga = getManga(c, settings);

      Div grid = new Div();
      grid.addClassName("grid");

      for (Manga m : manga) {
        MangaCard card = new MangaCard(settings, m);
        grid.add(card);
      }

      if (c.getId() != 0) {
        Button deleteButton = new Button(VaadinIcon.TRASH.create());

        deleteButton.addClassName("delete-category-button");

        deleteButton.addClickListener(e -> {
          Settings s = SerializationUtils.deseralizeSettings();
          if (CategoryUtils.deleteCategory(client, s, c.getId())) {
            tabs.remove(tab);
          } else {
            Notification notification = new Notification("Failed to delete category", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
          }
        });

        tab.add(deleteButton);
      }

      tabs.add(tab, grid);
    }
  }

}
