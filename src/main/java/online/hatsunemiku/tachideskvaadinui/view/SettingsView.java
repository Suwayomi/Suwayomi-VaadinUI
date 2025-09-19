/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.TemporaryFileUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.open.OSUtils;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import online.hatsunemiku.tachideskvaadinui.data.settings.FlareSolverrSettings;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.SettingsEventPublisher;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.ExtensionRepo;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.services.SuwayomiSettingsService;
import online.hatsunemiku.tachideskvaadinui.services.notification.WebPushService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.vaadin.miki.superfields.checkbox.SuperCheckbox;
import org.vaadin.miki.superfields.text.SuperTextField;

/**
 * This class represents a view for the settings of the application. It includes functionality for
 * changing the settings of the application.
 *
 * @author aless2003
 * @version 1.2.0
 * @since 1.0.0
 */
@Route("settings")
@CssImport("./css/views/settings-view.css")
public class SettingsView extends StandardLayout {

  public static final String STARTUP_CMD_NAME = "startupVaaUI.cmd";
  private static final Logger log = LoggerFactory.getLogger(SettingsView.class);
  private static final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
  private final SettingsEventPublisher settingsEventPublisher;
  private final SuwayomiSettingsService suwayomiSettingsService;
  private final WebPushService webPushService;

  /**
   * Creates a new instance of the {@link SettingsView} class.
   *
   * @param settingsService The service to retrieve settings from.
   * @param eventPublisher The event publisher to publish settings events with.
   * @param sourceService The service to retrieve sources from.
   * @param suwayomiSettingsService The service to retrieve Suwayomi settings from.
   */
  public SettingsView(
      SettingsService settingsService,
      SettingsEventPublisher eventPublisher,
      SourceService sourceService,
      SuwayomiSettingsService suwayomiSettingsService,
      WebPushService webPushService) {
    super("Settings");
    setClassName("settings-view");

    this.settingsEventPublisher = eventPublisher;
    this.suwayomiSettingsService = suwayomiSettingsService;

    VerticalLayout content = new VerticalLayout();
    content.setClassName("settings-content");

    Section generalSettings = getGeneralSettingsSection(settingsService, sourceService);
    Section flareSolverrSettings = createFlareSolverrSection();
    Div separator = getSeparator();
    Section extensionSettings = getExtensionSettingsSection();
    Section notificationSettings = createNotificationSettingsSection();
    Section backupSection = getBackupSection(settingsService);
    content.add(
        generalSettings,
        getSeparator(),
        flareSolverrSettings,
        separator,
        extensionSettings,
        getSeparator(),
        notificationSettings,
        getSeparator(),
        backupSection
        );

    setContent(content);
    this.webPushService = webPushService;
  }

  /**
   * This method is used to create a button to cancel editing.
   *
   * @param editor The editor to cancel.
   * @return A {@link Button} to cancel editing.
   */
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

  /** Removes the batch file in the Windows startup folder that starts the Vaadin UI on startup. */
  private static void removeWindowsStartup() {
    if (!OSUtils.isWindows()) {
      Notification notification =
          new Notification("Startup with Windows is only available on Windows", 3000);
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.open();
      return;
    }

    try {
      var startupFolder = getStartupFolder();
      var startupShortcut = new File(startupFolder, STARTUP_CMD_NAME);
      Files.deleteIfExists(startupShortcut.toPath());
      Notification notification = new Notification("Startup shortcut removed", 3000);
      notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      notification.open();
    } catch (IOException ex) {
      log.error("Failed to remove startup shortcut", ex);
      var startupFolder = getStartupFolder();
      var startupShortcut = new File(startupFolder, STARTUP_CMD_NAME);
      startupShortcut.deleteOnExit();

      Notification notification =
          new Notification(
              "Failed to remove startup shortcut. Trying to remove shortcut after Application"
                  + " closes",
              3000);
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.open();
    }
  }

  /**
   * Gets the Windows startup folder.
   *
   * @return The path to the Windows startup folder as a String.
   */
  private static @NotNull String getStartupFolder() {
    String appdata = System.getenv("APPDATA");
    String startupShellPath = "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
    return appdata + startupShellPath;
  }

  /**
   * This method creates the UI section for the settings related to notifications.
   *
   * @return A {@link Section} element containing the notification settings UI.
   */
  private Section createNotificationSettingsSection() {
    Section section = new Section();
    section.setId("notification-settings-section");

    var buttons = new Div();
    buttons.setId("notification-buttons");

    H2 header = new H2("Notifications");
    header.addClassName("settings-header");

    Button subscribeButton = new Button("Subscribe to notifications");
    subscribeButton.addClickListener(
        event -> {
          var ui = getUI().orElse(UI.getCurrent());
          webPushService.subscribe(ui);
          Notification notification = new Notification("Subscribed to notifications", 3000);
          notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          notification.open();
        });

    Button testNotificationButton = new Button("Test notification");
    testNotificationButton.addClickListener(
        event -> {
          webPushService.notify("Test Title", "Test Message");
          Notification notification = new Notification("Test notification sent", 3000);
          notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          notification.open();
        });

    Button unsubscribeButton = new Button("Unsubscribe from notifications");
    unsubscribeButton.addClickListener(
        event -> {
          webPushService.removeSubscription();
          Notification notification = new Notification("Unsubscribed from notifications", 3000);
          notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          notification.open();
        });

    buttons.add(subscribeButton, testNotificationButton, unsubscribeButton);

    section.add(header, buttons);
    return section;
  }

  /**
   * This method is used to create a section for the general settings.
   *
   * @param settingsService The service to retrieve settings from.
   * @param sourceService The service to retrieve sources from.
   * @return A {@link Section} containing the general settings.
   */
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

    checkbox.addClassName("settings-checkbox");
    checkbox.setValue(settingsService.getSettings().isStartPopup());
    binder.forField(checkbox).bind(Settings::isStartPopup, Settings::setStartPopup);
    checkboxContainer.add(checkbox);

    if (OSUtils.isWindows()) {
      var startupWithWindowsCheckbox = getStartupWithWindowsCheckbox(settingsService, binder);
      checkboxContainer.add(startupWithWindowsCheckbox);
    }

    binder.setBean(settingsService.getSettings());

    generalSettingsContent.add(urlField, defaultSearchLangField, defaultSourceField);
    generalSettingsContent.add(checkboxContainer, 2);

    generalSettingsSection.add(header, generalSettingsContent);
    return generalSettingsSection;
  }

  /**
   * This method creates the checkbox to let the user choose whether to start the application with
   * Windows.
   *
   * @param settingsService The service to retrieve settings from.
   * @param binder The binder to bind the checkbox to the startWithWindows property of the Settings
   *     object.
   * @return The configured {@link SuperCheckbox} element.
   */
  private @NotNull SuperCheckbox getStartupWithWindowsCheckbox(
      SettingsService settingsService, Binder<Settings> binder) {
    SuperCheckbox startupWithWindowsCheckbox =
        new SuperCheckbox().withLabel("Start with Windows").withId("start-with-windows");
    startupWithWindowsCheckbox.setValue(settingsService.getSettings().isStartWithWindows());
    startupWithWindowsCheckbox.addClassName("settings-checkbox");
    startupWithWindowsCheckbox.addValueChangeListener(
        e -> {
          boolean startWithWindows = e.getValue();
          if (startWithWindows) {
            createWindowsStartup();
          } else {
            removeWindowsStartup();
          }
        });
    binder
        .forField(startupWithWindowsCheckbox)
        .bind(Settings::isStartWithWindows, Settings::setStartWithWindows);
    return startupWithWindowsCheckbox;
  }

  /** Creates a batch file in the Windows startup folder to start the Vaadin UI on startup. */
  private void createWindowsStartup() {
    if (!OSUtils.isWindows()) {
      Notification notification =
          new Notification("Startup with Windows is only available on Windows", 3000);
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.open();
      return;
    }

    try {
      var vaauiDir = getVaaUIDir();

      var exeFile = new File(vaauiDir, "Tachidesk Vaadin UI.exe");
      log.debug("Exe file: {}", exeFile);

      if (!exeFile.exists()) {
        log.error("Tachidesk Vaadin UI.exe not found");
        Notification notification = new Notification("Tachidesk Vaadin UI.exe not found", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
        return;
      }

      String startupFolder = getStartupFolder();
      File startupFolderFile = new File(startupFolder);

      if (!startupFolderFile.exists()) {
        log.error("Startup folder not found");
        log.info("startup folder: {}", startupFolder);
        Notification notification = new Notification("Startup folder not found", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
        return;
      }

      File startupShortcut = new File(startupFolderFile, STARTUP_CMD_NAME);
      String startupCommand =
          """
          @echo off
          chdir "%s"
          start "" "%s"
          """
              .formatted(vaauiDir.getAbsolutePath(), exeFile.getAbsolutePath());

      Files.deleteIfExists(startupShortcut.toPath());
      Files.createFile(startupShortcut.toPath());
      Files.writeString(startupShortcut.toPath(), startupCommand);

      Notification notification = new Notification("Startup shortcut created", 3000);
      notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      notification.open();
    } catch (IOException ex) {
      log.error("Failed to create startup shortcut", ex);
    } catch (RuntimeException ex) {
      log.error("Failed to create startup shortcut. Reason: {}", ex.getMessage());
    }
  }

  /**
   * Gets the directory where the VaadinUI installation is located.
   *
   * @return The directory where the VaadinUI installation is located.
   */
  private File getVaaUIDir() {
    var jarLocation = getJarLocation();

    String normalJarPath = URLDecoder.decode(jarLocation, StandardCharsets.UTF_8);

    File jarFile = new File(normalJarPath);

    var vaauiDir = jarFile.getParentFile().getParentFile();
    log.debug("VaaUI dir: {}", vaauiDir);
    return vaauiDir;
  }

  /**
   * Gets the location of the running jar file. Throws an exception if there's no jar file.
   *
   * @return The path of the running jar file as a String.
   */
  private @NotNull String getJarLocation() {
    var jarLocation =
        this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();

    if (!jarLocation.startsWith("jar")) {
      Notification notification = new Notification("Not running from a jar file", 3000);
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.open();
      throw new IllegalStateException("Not running from a jar file");
    }
    log.debug("Code location: {}", jarLocation);
    jarLocation = jarLocation.replace("jar:nested:/", "");
    jarLocation = jarLocation.replace("!BOOT-INF/classes/!/", "");

    log.debug("Jar location: {}", jarLocation);
    return jarLocation;
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

  /**
   * This method is used to create a ComboBox of languages available from the sources to set as
   * default search language.
   *
   * @param sourceService The service to retrieve sources from.
   * @param binder The binder to bind the selected language to the defaultSearchLang property
   * @return A {@link ComboBox} of available languages, or a read-only ComboBox with a warning
   *     message if the server is not running.
   */
  private ComboBox<String> createSearchLangField(
      SourceService sourceService, Binder<Settings> binder) {

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

  /**
   * This method is used to get a {@link ComboBox} of languages available from the sources to set as
   * default source language.
   *
   * @param sourceService The service to retrieve sources from.
   * @param binder The binder to bind the selected source to the defaultSourceLang property of the
   *     Settings object.
   * @return A {@link ComboBox} of available languages, or a read-only ComboBox with a warning
   *     message if the server is not running.
   */
  private ComboBox<String> getDefaultSourceField(
      SourceService sourceService, Binder<Settings> binder) {
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

  /**
   * This method is used to get a ComboBox of languages available from the sources.
   *
   * @param sourceService The service to retrieve sources from.
   * @return A {@link ComboBox} of languages available from the sources, or a read-only ComboBox
   *     with a warning message if the server is not running.
   */
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

  /**
   * This method is used to create a section for the settings specific to FlareSolverr.
   *
   * @return A {@link Section} containing the settings for FlareSolverr.
   */
  private Section createFlareSolverrSection() {
    Section section = new Section();
    section.addClassName("flare-solverr-settings");

    FormLayout form = new FormLayout();

    FlareSolverrSettings flareSolverrSettings;
    try {
      flareSolverrSettings = suwayomiSettingsService.getFlareSolverrSettings();
    } catch (Exception e) {
      flareSolverrSettings = null;
    }

    TextField urlField = createFlareSolverrUrlField(flareSolverrSettings);

    ComboBox<Boolean> enabledChoiceBox = createFlareSolverrEnabledField(flareSolverrSettings);
    form.add(urlField);
    form.add(enabledChoiceBox);

    section.add(form);

    return section;
  }

  /**
   * This method is used to create a TextField to set the URL of the FlareSolverr server.
   *
   * @param flareSolverrSettings The {@link FlareSolverrSettings FlareSolverr Settings} representing
   *     the initial state.
   * @return A {@link TextField} to set the URL of the FlareSolverr server.
   */
  private @NotNull TextField createFlareSolverrUrlField(FlareSolverrSettings flareSolverrSettings) {
    TextField urlField = new TextField("FlareSolverr URL");
    if (flareSolverrSettings != null) {
      urlField.setValue(flareSolverrSettings.getUrl());
    } else {
      urlField.setEnabled(false);
      urlField.setValue("Not available, because server is not running");
    }
    urlField.setPlaceholder("http://localhost:8191");
    urlField.addValueChangeListener(
        e -> {
          String url = e.getValue();
          if (url == null || url.isBlank()) {
            return;
          }

          try {
            boolean success = suwayomiSettingsService.updateFlareSolverrUrl(url);

            Notification notification;
            if (!success) {
              notification = new Notification("Failed to update FlareSolverr URL", 3000);
              notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
              notification = new Notification("FlareSolverr URL updated", 3000);
              notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            notification.open();
          } catch (IllegalArgumentException ex) {
            log.error("Invalid URL", ex);
            Notification notification = new Notification("Invalid URL", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();
          }
        });
    return urlField;
  }

  /**
   * This method is used to create a ComboBox to enable or disable FlareSolverr.
   *
   * @param flareSolverrSettings The {@link FlareSolverrSettings FlareSolverr Settings} representing
   *     the initial state.
   * @return A {@link ComboBox} to enable or disable FlareSolverr.
   */
  private @NotNull ComboBox<Boolean> createFlareSolverrEnabledField(
      FlareSolverrSettings flareSolverrSettings) {
    ComboBox<Boolean> enabledChoiceBox = new ComboBox<>();
    enabledChoiceBox.setLabel("FlareSolverr Enabled");
    if (flareSolverrSettings != null) {
      enabledChoiceBox.setItems(true, false);
      enabledChoiceBox.setValue(flareSolverrSettings.isEnabled());
    } else {
      enabledChoiceBox.setEnabled(false);
    }

    enabledChoiceBox.addValueChangeListener(
        e -> {
          boolean enabled = e.getValue();
          boolean success = suwayomiSettingsService.updateFlareSolverrEnabledStatus(enabled);

          Notification notification;
          if (!success) {
            notification = new Notification("Failed to update FlareSolverr enabled status", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
          } else {
            notification = new Notification("FlareSolverr enabled status updated", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          }
          notification.open();
        });
    return enabledChoiceBox;
  }

  private @NotNull Section getBackupSection(SettingsService service) {
    Section section = new Section();
    section.addClassName("backup-settings");

    Div content = new Div();
    content.setId("backup-settings-content");
    var header = new H2("Backup");
    header.setId("backup-settings-header");

    Div buttons = new Div();
    buttons.setId("backup-settings-buttons");

    var settings = service.getSettings();
    Button backupButton = new Button("Create Backup");
    backupButton.addClickListener(
        event -> {
          try {
            var downloadUrl = suwayomiSettingsService.createBackup(settings.getUrl());
            UI.getCurrent().getPage().open(downloadUrl, "_blank");
            Notification notification = new Notification("Backup created", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();
          } catch (Exception e) {
            log.error("Failed to backup", e);
            Notification notification = new Notification("Failed to backup", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
          }
        }
    );

    AtomicReference<Path> backupFile = new AtomicReference<>();
    UploadHandler uploadHandler = new TemporaryFileUploadHandler((metadata, file) -> {
      backupFile.set(file.toPath());
    });

    Upload upload = new Upload(uploadHandler);
    upload.setAutoUpload(true);
    upload.setMaxFiles(1);

    Button restore = new Button("Restore Backup");
    restore.addClickListener(e -> {
      log.info("Restoring backup");
      if (backupFile.get() == null) {
        Notification notification = new Notification("No backup file selected", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
        return;
      }

      boolean restoredBackup = suwayomiSettingsService.restoreBackup(backupFile.get());

      Notification notification;
      if (restoredBackup) {
        notification = new Notification("Backup restored", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      } else {
        notification = new Notification("Failed to restore backup", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
      notification.open();
    });

    Div restorePart = new Div();
    restorePart.setId("backup-restore");
    restorePart.add(upload, restore);

    buttons.add(backupButton, restorePart);

    content.add(header, buttons);

    section.add(content);

    return section;
  }
}
