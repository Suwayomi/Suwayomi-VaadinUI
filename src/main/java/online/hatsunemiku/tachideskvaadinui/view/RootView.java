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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import online.hatsunemiku.tachideskvaadinui.view.trackers.AniListView;
import online.hatsunemiku.tachideskvaadinui.view.trackers.MALView;
import org.jetbrains.annotations.NotNull;


@Route("/")
@CssImport("./css/root.css")
public class RootView extends StandardLayout implements BeforeEnterObserver {

  private TabSheet tabs;
  private Span activeCategoryLabel;
  private Span seriesCountLabel;
  private final Map<Category, Integer> categoryMangaCounts = new HashMap<>();
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

    categoryMangaCounts.put(c, manga.size());

    Div grid = new Div();
    grid.addClassName("library-grid");

    fillMangaGrid(settings, manga, grid, c);
    return grid;
  }

  private void fillMangaGrid(Settings settings, List<Manga> manga, Div grid, Category c) {
    for (Manga m : manga) {
      MangaCard card = new DraggableMangaCard(settings, m, c, this.mangaService);
      grid.add(card);
    }
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {

    fullScreenNoHide(); // Remove default StandardLayout padding/margins to let the grid expand
    addClassName("library-screen");

    List<Category> categories;

    try {
      categories = categoryService.getCategories();
    } catch (Exception e) {
      UI ui = UI.getCurrent();
      ui.access(() -> ui.navigate(ServerStartView.class));
      return;
    }

    activeCategoryLabel = new Span();
    activeCategoryLabel.setClassName("active-category-label");
    seriesCountLabel = new Span();
    seriesCountLabel.setClassName("series-count-label");

    tabs = new TabSheet();
    tabs.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
    tabs.addClassName("library-tabsheet");
    tabs.setSizeFull();
    addCategoryTabs(categories, settingsService.getSettings());

    tabs.addSelectedChangeListener(
        e -> {
          Tab selectedTab = e.getSelectedTab();
          if (selectedTab instanceof CategoryTab categoryTab) {
            updateCategoryHeader(categoryTab.getCategory());
          }
        });

    // Initial update for the first tab
    if (!categories.isEmpty()) {
      updateCategoryHeader(categories.getFirst());
    }

    Div buttons = getTabSheetButtons();
    buttons.addClassName("library-action-buttons");

    Div categoryInfo = new Div(activeCategoryLabel, seriesCountLabel);
    categoryInfo.setClassName("library-category-info");

    HorizontalLayout header = new HorizontalLayout(categoryInfo, buttons);
    header.setClassName("library-header");
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);

    Div container = new Div(header, tabs);
    container.setClassName("library-view-container");
    container.setSizeFull(); // Ensure container takes up full available height/width
    container.getStyle().set("display", "flex");
    container.getStyle().set("flex-direction", "column");
    container.getStyle().set("flex", "1 1 auto");
    container.getStyle().set("min-height", "0");

    setContent(container);
  }


  private void updateCategoryHeader(Category category) {
    activeCategoryLabel.setText(category.getName());
    Integer count = categoryMangaCounts.getOrDefault(category, 0);
    seriesCountLabel.setText(count + " SERIES");
  }

  @NotNull
  private Div getTabSheetButtons() {
    Div buttons = new Div();
    buttons.setClassName("library-buttons");

    Button createButton = new Button("Add Category", VaadinIcon.PLUS.create());
    createButton.addClassName("add-category-button");
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
                updateCategoryHeader(c);
              });

          dialog.open();
        });

    Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
    refreshButton.addClassName("refresh-library-button");
    refreshButton.addClickListener(
        e -> {
          UI ui = getUI().orElse(UI.getCurrent());

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

    Button aniListImportBtn = getALImportBtn();
    Button malImportBtn = getMalImportBtn();

    buttons.add(aniListImportBtn, malImportBtn, refreshButton, createButton);
    return buttons;
  }

  @NotNull
  private Button getMalImportBtn() {
    Button malImportBtn = new Button("Import from MAL", VaadinIcon.DOWNLOAD.create());
    malImportBtn.addClassName("mal-import-button");
    malImportBtn.addClickListener(
        e -> {
          UI ui = getUI().orElse(UI.getCurrent());
          if (ui != null) {
            ui.navigate(MALView.class);
          }
        });
    return malImportBtn;
  }

  @NotNull
  private Button getALImportBtn() {
    Button importBtn = new Button("Import from AniList", VaadinIcon.DOWNLOAD.create());
    importBtn.addClassName("anilist-import-button");
    importBtn.addClickListener(
        e -> {
          UI ui = getUI().orElse(UI.getCurrent());
          if (ui != null) {
            ui.navigate(AniListView.class);
          }
        });
    return importBtn;
  }
}

