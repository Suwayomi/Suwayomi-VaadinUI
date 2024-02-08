/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.services.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import org.jetbrains.annotations.NotNull;
import org.vaadin.miki.shared.labels.LabelPosition;
import org.vaadin.miki.superfields.text.LabelField;

public class TrackingMangaChoiceDialog extends Dialog {

  public TrackingMangaChoiceDialog(
      String mangaName,
      long mangaId,
      AniListAPIService aniListAPI,
      TrackingDataService dataService) {

    this.setClassName("tracking-manga-choice-dialog");

    TextField searchField = new TextField("Search Manga");
    searchField.setValue(mangaName);

    var apiResponse = aniListAPI.searchManga(mangaName);
    var mangaList = apiResponse.data().page().media();

    AtomicReference<AniListMedia> selectedManga = new AtomicReference<>();

    Div noResults = new Div("No results found");
    noResults.setId("no-search-results-text");

    ListBox<AniListMedia> searchResults = new ListBox<>();
    searchResults.addClassName("manga-search-results");
    searchResults.setRenderer(getRenderer());
    searchResults.setItems(mangaList);
    searchResults
        .getDataProvider()
        .addDataProviderListener(
            e -> changeSearchResultsVisibility(mangaList, searchResults, noResults));

    searchResults.addValueChangeListener(
        e -> {
          AniListMedia selected = e.getValue();

          if (selected == null) {
            return;
          }

          selectedManga.set(selected);
        });

    searchField.addValueChangeListener(
        e -> {
          String value = e.getValue();

          if (value.isBlank()) {
            searchField.setValue(e.getOldValue());
            return;
          }

          var response = aniListAPI.searchManga(value);
          mangaList.clear();

          mangaList.addAll(response.data().page().media());

          searchResults.getDataProvider().refreshAll();
        });

    changeSearchResultsVisibility(mangaList, searchResults, noResults);

    add(searchField, searchResults, noResults);

    Checkbox privateCheckbox = new Checkbox("Private");

    var buttons = new Div();

    Button closeBtn = new Button("Close");
    closeBtn.addClickListener(e -> close());

    Button saveBtn = new Button("Save");
    saveBtn.addClickListener(
        e -> {
          var manga = selectedManga.get();

          if (manga == null) {
            Notification.show("Please select a manga to save");
            return;
          }
          int aniListId = manga.id();

          boolean isPrivate = privateCheckbox.getValue();

          if (!aniListAPI.isMangaInList(aniListId)) {
            aniListAPI.addMangaToList(aniListId, isPrivate);
          }

          dataService.getTracker(mangaId).setAniListId(aniListId);
          dataService.getTracker(mangaId).setPrivate(privateCheckbox.getValue());

          close();
        });

    buttons.add(closeBtn, saveBtn);

    var footer = getFooter();

    footer.add(privateCheckbox, buttons);
  }

  private static void changeSearchResultsVisibility(
      List<AniListMedia> mangaList, ListBox<AniListMedia> searchResults, Div noResults) {
    if (mangaList.isEmpty()) {
      searchResults.setVisible(false);
      noResults.setVisible(true);
    } else {
      searchResults.setVisible(true);
      noResults.setVisible(false);
    }
  }

  @NotNull
  private static ComponentRenderer<Component, AniListMedia> getRenderer() {
    return new ComponentRenderer<>(
        media -> {
          Div content = new Div();
          content.setClassName("manga-search-result");

          MediaDate mangaDate = media.date();

          Div upperHalf = new Div();
          upperHalf.setClassName("manga-search-result-upper-half");

          Image image = new Image(media.coverImage().large(), "Cover Image");

          Div data = new Div();

          LabelField<String> title =
              new LabelField<String>()
                  .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
                  .withLabel("Title")
                  .withValue(media.title().userPreferred());

          title.setClassName("manga-search-result-attribute");

          LabelField<String> type =
              new LabelField<String>()
                  .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
                  .withLabel("Type")
                  .withValue(media.format());

          type.addClassName("manga-search-result-attribute");

          String date = "%s-%s-%s".formatted(mangaDate.year(), mangaDate.month(), mangaDate.day());
          LabelField<String> started =
              new LabelField<String>()
                  .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
                  .withLabel("Started")
                  .withValue(date);

          started.addClassName("manga-search-result-attribute");

          LabelField<String> status =
              new LabelField<String>()
                  .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
                  .withLabel("Status")
                  .withValue(media.status());

          status.addClassName("manga-search-result-attribute");

          data.add(title, type, started, status);
          data.setClassName("manga-search-result-data");

          upperHalf.add(image, data);

          Text description = new Text(media.description());

          content.add(upperHalf, description);

          return content;
        });
  }
}
