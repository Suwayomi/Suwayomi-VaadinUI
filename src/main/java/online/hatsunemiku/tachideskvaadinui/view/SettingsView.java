/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.SettingsEventPublisher;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ExtensionRepo;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.services.SuwayomiSettingsService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.vaadin.miki.superfields.checkbox.SuperCheckbox;
import org.vaadin.miki.superfields.text.SuperTextField;

@Route("settings")
@CssImport("./css/views/settings-view.css")
public class SettingsView extends StandardLayout {

  private static final Logger log = LoggerFactory.getLogger(SettingsView.class);
  private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
  private final SettingsEventPublisher settingsEventPublisher;
  private final SuwayomiSettingsService suwayomiSettingsService;

  public SettingsView(
      SettingsService settingsService,
      SettingsEventPublisher eventPublisher,
      SourceService sourceService,
      SuwayomiSettingsService suwayomiSettingsService) {
    super("Settings");
    setClassName("settings-view");

    this.settingsEventPublisher = eventPublisher;
    this.suwayomiSettingsService = suwayomiSettingsService;

    VerticalLayout content = new VerticalLayout();
    content.setClassName("settings-content");

    Section generalSettings = getGeneralSettingsSection(settingsService, sourceService);
    Div separator = getSeparator();
    Section extensionSettings = getExtensionSettingsSection();

    content.add(generalSettings, separator, extensionSettings);

    setContent(content);
  }

  @NotNull
  private static Button getEditingCancelBtn(Editor<ExtensionRepo> editor) {
    Button cancelButton = new Button("Cancel");
    cancelButton.addClickListener(
        event -> {
          if (!editor.isOpen()) {
            return;
          }

          editor.cancel();
        });
    return cancelButton;
  }

  @NotNull
  private Section getGeneralSettingsSection(
      SettingsService settingsService, SourceService sourceService) {
    Section generalSettingsSection = new Section();
    generalSettingsSection.setId("general-settings-section");

    var header = new H2("General");
    header.addClassName("settings-header");

    FormLayout generalSettingsContent = new FormLayout();

    Binder<Settings> binder = new Binder<>(Settings.class);
    SuperTextField urlField = createUrlFieldWithValidation(binder);
    var defaultSearchLangField = createSearchLangField(sourceService, binder);
    var defaultSourceField = getDefaultSourceField(sourceService, binder);

    Div checkboxContainer = new Div();
    checkboxContainer.addClassName("checkbox-container");

    SuperCheckbox checkbox = new SuperCheckbox().withLabel("Startup Popup").withId("start-popup");
    checkbox.setValue(settingsService.getSettings().isStartPopup());
    binder.forField(checkbox).bind(Settings::isStartPopup, Settings::setStartPopup);

    checkboxContainer.add(checkbox);

    binder.setBean(settingsService.getSettings());

    generalSettingsContent.add(urlField, defaultSearchLangField, defaultSourceField);
    generalSettingsContent.add(checkboxContainer, 2);

    generalSettingsSection.add(header, generalSettingsContent);
    return generalSettingsSection;
  }

  private Section getExtensionSettingsSection() {
    Section extensionSettings = new Section();
    extensionSettings.setId("extension-settings-section");

    Div extensionSettingsContent = new Div();
    extensionSettingsContent.setId("extension-settings");

    var header = new H2("Extensions");
    header.addClassName("settings-header");

    String descriptionText =
        """
            So, because there's people who abuse DMCA to take down repos I can't include any extension repos by default.
            However, you can add them yourself here. Just paste the URL of the repo where the extensions are stored in the box below.
            Now the obligatory disclaimer: I'm not responsible for any malicious code that might be in the extensions you install.
            I also do not condone piracy, etc. etc. etc. blah blah blah. You know the drill.
            If you don't know where to find extension repos, you can find some on github.
            Some that were brought up during a discussion are https://github.com/keiyoushi/extensions and https://github.com/ThePBone/tachiyomi-extensions-revived.
            I do not endorse them, I'm merely mentioning them as examples ;)
            """;

    Span description = new Span(descriptionText);
    description.setId("extension-settings-description");

    Grid<ExtensionRepo> extensionReposList;
    try {
      extensionReposList = createExtensionReposList();
    } catch (Exception e) {
      Div noServerWarning = new Div();
      noServerWarning.setId("no-server-warning");

      String serverNotRunningMessage =
          "Server is not running, please wait until it's running to add extension repos";

      Text noServerWarningText = new Text(serverNotRunningMessage);
      noServerWarning.add(noServerWarningText);

      extensionSettingsContent.add(header, description, noServerWarning);
      extensionSettings.add(extensionSettingsContent);

      return extensionSettings;
    }

    extensionSettingsContent.add(header, description, extensionReposList);

    extensionSettings.add(extensionSettingsContent);

    return extensionSettings;
  }

  private Div getSeparator() {
    Div separator = new Div();
    separator.addClassName("separator");
    return separator;
  }

  private Grid<ExtensionRepo> createExtensionReposList() {
    Grid<ExtensionRepo> repoGrid = new Grid<>();
    Binder<ExtensionRepo> binder = new Binder<>(ExtensionRepo.class);
    var editor = repoGrid.getEditor();
    editor.setBinder(binder);
    editor.setBuffered(true);

    TextField extensionRepoUrlField = new TextField("Extension Repo URL");
    extensionRepoUrlField.getStyle().set("width", "90%");

    var url = repoGrid.addColumn(ExtensionRepo::getUrl).setHeader("Extension Repos");
    url.setEditorComponent(extensionRepoUrlField);

    var headRow = repoGrid.getHeaderRows().get(0);
    var addRepoButton = new Button("Add", VaadinIcon.PLUS.create());

    repoGrid.setAllRowsVisible(true);
    var buttonColumn =
        repoGrid.addComponentColumn(
            extensionRepo -> {
              var editButton = new Button(VaadinIcon.EDIT.create());
              editButton.addClickListener(
                  buttonClickEvent -> {
                    if (editor.isOpen()) {
                      editor.cancel();
                    }

                    log.info("Opening Editor");
                    editor.editItem(extensionRepo);
                    extensionRepoUrlField.focus();
                  });

              var deleteButton = new Button(VaadinIcon.TRASH.create());
              deleteButton.addClickListener(
                  event -> {
                    suwayomiSettingsService.removeExtensionRepo(extensionRepo.getUrl());
                    reloadGrid(repoGrid);
                  });

              var buttonContainer = new Div();
              buttonContainer.addClassName("right-align");
              buttonContainer.addClassName("button-container");
              buttonContainer.add(editButton, deleteButton);

              return buttonContainer;
            });

    Div editingMenu = getEditingMenu(editor, repoGrid);
    buttonColumn.setEditorComponent(editingMenu);

    editor.addSaveListener(
        event -> {
          var newRepo = event.getItem();
          suwayomiSettingsService.addExtensionRepo(newRepo.getUrl());
        });

    binder
        .forField(extensionRepoUrlField)
        .withValidator(
            (input, context) -> {
              if (input == null || input.isEmpty()) {
                return ValidationResult.error("URL cannot be empty");
              }

              if (!input.startsWith("http") && !input.startsWith("https")) {
                return ValidationResult.error("URL must start with http:// or https://");
              }

              UrlValidator urlValidator = new UrlValidator();

              if (!urlValidator.isValid(input)) {
                return ValidationResult.error("URL is not valid");
              }

              return ValidationResult.ok();
            })
        .bind(ExtensionRepo::getUrl, ExtensionRepo::setUrl);

    var extensionRepos = suwayomiSettingsService.getExtensionRepos();

    HeaderRow.HeaderCell buttonCell = headRow.getCell(buttonColumn);
    Div buttonContainer = new Div(addRepoButton);
    buttonContainer.addClassName("right-align");
    buttonCell.setComponent(buttonContainer);

    Dialog dialog = new Dialog();
    SuperTextField addRepoUrlField = new SuperTextField("Extension Repo URL");
    dialog.add(addRepoUrlField);
    Div dialogButtons = getDialogButtons(dialog, addRepoUrlField, repoGrid);
    dialog.add(dialogButtons);

    addRepoButton.addClickListener(
        event -> {
          dialog.open();
          addRepoUrlField.focus();
        });

    repoGrid.setItems(extensionRepos);
    repoGrid.setId("extension-repos-grid");

    return repoGrid;
  }

  @NotNull
  private Div getEditingMenu(Editor<ExtensionRepo> editor, Grid<ExtensionRepo> repoGrid) {
    Button saveButton = getEditingSaveBtn(editor, repoGrid);

    Button cancelButton = getEditingCancelBtn(editor);

    Div editingMenu = new Div();
    editingMenu.addClassNames("right-align", "button-container");
    editingMenu.add(saveButton, cancelButton);
    return editingMenu;
  }

  @NotNull
  private Button getEditingSaveBtn(Editor<ExtensionRepo> editor, Grid<ExtensionRepo> repoGrid) {
    Button saveButton = new Button("Save");
    saveButton.addClickListener(
        buttonClickEvent -> {
          if (!editor.isOpen()) {
            return;
          }
          log.info("Saving");
          var oldUrl = editor.getItem().getUrl();
          boolean success = editor.save();
          if (success) {
            log.debug("Couldn't save extension repo");
            suwayomiSettingsService.removeExtensionRepo(oldUrl);
          } else {
            log.debug("Saved extension repo");
          }

          reloadGrid(repoGrid);
        });
    return saveButton;
  }

  @NotNull
  private Div getDialogButtons(
      Dialog dialog, SuperTextField addRepoUrlField, Grid<ExtensionRepo> repoGrid) {

    Div dialogButtons = new Div();
    dialogButtons.setId("dialog-buttons");

    Button cancelButton = new Button("Cancel");
    cancelButton.addClickListener(event -> dialog.close());

    Button addButton = new Button("Add");
    addButton.addClickListener(
        event -> {
          String newRepoUrl = addRepoUrlField.getValue();
          if (newRepoUrl == null || newRepoUrl.isEmpty()) {
            return;
          }

          if (!newRepoUrl.startsWith("http") && !newRepoUrl.startsWith("https")) {
            return;
          }

          UrlValidator urlValidator = new UrlValidator();

          if (!urlValidator.isValid(newRepoUrl)) {
            return;
          }

          suwayomiSettingsService.addExtensionRepo(newRepoUrl);
          dialog.close();
          reloadGrid(repoGrid);
        });

    addRepoUrlField.addKeyPressListener(Key.ENTER, event -> addButton.click());

    dialogButtons.add(addButton, cancelButton);
    return dialogButtons;
  }

  private void reloadGrid(Grid<ExtensionRepo> grid) {
    var extensionRepos = suwayomiSettingsService.getExtensionRepos();
    grid.setItems(extensionRepos);
  }

  private ComboBox<String> createSearchLangField(SourceService sourceService,
      Binder<Settings> binder) {

    var defaultSearchLang = getDefaultLangField(sourceService);
    defaultSearchLang.setLabel("Default Search Language");

    binder
        .forField(defaultSearchLang)
        .withValidator(
            (lang, context) -> {
              if (lang == null || lang.isEmpty()) {
                return ValidationResult.error("Default Search Language cannot be empty");
              }

              if (lang.equals("Loading...")) {
                return ValidationResult.error("Default Search Language cannot be Loading...");
              }

              return ValidationResult.ok();
            })
        .bind(Settings::getDefaultSearchLang, Settings::setDefaultSearchLang);

    return defaultSearchLang;
  }

  @NotNull
  private SuperTextField createUrlFieldWithValidation(Binder<Settings> binder) {
    SuperTextField urlField = new SuperTextField("URL");
    binder
        .forField(urlField)
        .withValidator(
            (url, context) -> {
              if (url == null || url.isEmpty()) {
                return ValidationResult.error("URL cannot be empty");
              }

              if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return ValidationResult.error("URL must start with http:// or https://");
              }

              String urlWithoutPort = url.split(":\\d+")[0];

              if (!urlValidator.isValid(urlWithoutPort)) {
                return ValidationResult.error("URL is not valid");
              }

              return ValidationResult.ok();
            })
        .bind(
            Settings::getUrl,
            (settings, url) -> {
              settings.setUrl(url);
              settingsEventPublisher.publishUrlChangeEvent(this, url);
            });
    return urlField;
  }

  private ComboBox<String> getDefaultSourceField(SourceService sourceService,
      Binder<Settings> binder) {
    var defaultSource = getDefaultLangField(sourceService);
    defaultSource.setLabel("Default Source");

    if (defaultSource.isReadOnly()) {
      return defaultSource;
    }

    binder
        .forField(defaultSource)
        .withValidator(
            (source, context) -> {
              if (source == null || source.isEmpty()) {
                return ValidationResult.error("Default Source cannot be empty");
              }

              if (source.equals("Loading...")) {
                return ValidationResult.error("Default Source cannot be Loading...");
              }

              return ValidationResult.ok();
            })
        .bind(Settings::getDefaultSourceLang, Settings::setDefaultSourceLang);

    return defaultSource;
  }

  private ComboBox<String> getDefaultLangField(SourceService sourceService) {

    ComboBox<String> defaultLang = new ComboBox<>();
    defaultLang.setAllowCustomValue(false);
    List<Source> sources;
    try {
      sources = sourceService.getSources();
    } catch (ResourceAccessException e) {
      defaultLang.setReadOnly(true);
      defaultLang.setItems("Not available, because server is not running");
      defaultLang.setValue("Not available, because server is not running");
      return defaultLang;
    }

    var langs = new ArrayList<>(sources.stream().map(Source::getLang).distinct().toList());

    defaultLang.setItems(langs);

    return defaultLang;
  }
}
