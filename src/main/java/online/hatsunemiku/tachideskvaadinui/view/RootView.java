/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import online.hatsunemiku.tachideskvaadinui.component.card.DraggableMangaCard;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.component.dialog.category.CategoryDialog;
import online.hatsunemiku.tachideskvaadinui.component.tab.CategoryTab;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.services.CategoryService;
import online.hatsunemiku.tachideskvaadinui.services.LibUpdateService;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.jetbrains.annotations.NotNull;

@Route("/")
@CssImport("./css/root.css")
public class RootView extends StandardLayout implements BeforeEnterObserver {

  private TabSheet tabs;
  private final LibUpdateService libUpdateService;
  private final MangaService mangaService;
  private final CategoryService categoryService;
  private final SettingsService settingsService;
  private final ExecutorService updateExecutor;

  public RootView(
      SettingsService settingsService,
      LibUpdateService libUpdateService,
      MangaService mangaService,
      CategoryService categoryService) {
    super("Library");

    this.libUpdateService = libUpdateService;
    this.categoryService = categoryService;
    this.mangaService = mangaService;
    this.settingsService = settingsService;
    this.updateExecutor = Executors.newSingleThreadExecutor();
  }

  private void addCategoryTabs(List<Category> categories, Settings settings) {
    for (Category c : categories) {
      addCategoryTab(settings, c);
    }
  }

  private void addCategoryTab(Settings settings, Category c) {
    CategoryTab tab = new CategoryTab(c, mangaService);

    Div grid = createMangaGrid(settings, c);
    tab.setGrid(grid);

    if (c.getId() != 0) {
      Button deleteButton = createCategoryDeleteButton(c, tab);
      tab.add(deleteButton);
    }

    tabs.add(tab, grid);
  }

  @NotNull
  private Button createCategoryDeleteButton(Category c, Tab tab) {
    Button deleteButton = new Button(VaadinIcon.TRASH.create());

    deleteButton.addClassName("delete-category-button");

    deleteButton.addClickListener(
        e -> {
          if (categoryService.deleteCategory(c.getId())) {
            tabs.remove(tab);
          } else {
            Notification notification = new Notification("Failed to delete category", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
          }
        });
    return deleteButton;
  }

  @NotNull
  private Div createMangaGrid(Settings settings, Category c) {
    List<Manga> manga;
    try {
      manga = categoryService.getMangaFromCategory(c.getId());
    } catch (Exception e) {
      UI ui = UI.getCurrent();
      ui.access(() -> ui.navigate(ServerStartView.class));
      return new Div();
    }

    Div grid = new Div();
    grid.addClassName("library-grid");

    fillMangaGrid(settings, manga, grid, c);
    return grid;
  }

  private static void fillMangaGrid(Settings settings, List<Manga> manga, Div grid, Category c) {
    for (Manga m : manga) {
      MangaCard card = new DraggableMangaCard(settings, m, c);
      grid.add(card);
    }
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {

    List<Category> categories;

    try {
      categories = categoryService.getCategories();
    } catch (Exception e) {
      UI ui = UI.getCurrent();
      ui.access(() -> ui.navigate(ServerStartView.class));
      return;
    }

    tabs = new TabSheet();
    tabs.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
    addCategoryTabs(categories, settingsService.getSettings());

    Div buttons = getTabSheetButtons();
    tabs.setSuffixComponent(buttons);

    setContent(tabs);
  }

  @NotNull
  private Div getTabSheetButtons() {
    Div buttons = new Div();
    buttons.setClassName("library-buttons");

    Button createButton = new Button(VaadinIcon.PLUS.create());
    createButton.addClickListener(
        e -> {
          CategoryDialog dialog = new CategoryDialog(categoryService);

          dialog.addOpenedChangeListener(
              openedChangeEvent -> {
                if (!openedChangeEvent.isOpened()) {
                  removeClassName("blur");
                } else {
                  addClassName("blur");
                }
              });

          dialog.addOnCategoryCreationListener(
              categoryCreationEvent -> {
                Category c = categoryCreationEvent.getCategory();

                Settings s = settingsService.getSettings();

                addCategoryTab(s, c);
              });

          dialog.open();
        });

    Button refreshButton = new Button(VaadinIcon.REFRESH.create());
    refreshButton.addClickListener(
        e -> {
          UI ui = UI.getCurrent();

          updateExecutor.submit(
              () -> {
                ui.access(() -> e.getSource().setEnabled(false));

                boolean success;
                try {
                  success = this.libUpdateService.fetchUpdate(ui);
                } catch (IllegalStateException ex) {

                  ui.access(() -> e.getSource().setEnabled(true));

                  var notification =
                      new Notification(
                          "No Manga in Library", 5000, Notification.Position.BOTTOM_START);
                  notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                  ui.access(notification::open);

                  return;
                }
                Notification notification;
                if (!success) {
                  notification = new Notification("Failed to fetch update", 3000);
                  notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                } else {
                  notification = new Notification("Updated library", 3000);
                  notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                ui.access(notification::open);
                ui.access(() -> e.getSource().setEnabled(true));
              });
        });

    buttons.add(refreshButton, createButton);
    return buttons;
  }
}
