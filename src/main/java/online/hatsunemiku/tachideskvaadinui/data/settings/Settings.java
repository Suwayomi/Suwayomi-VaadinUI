/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

/**
 * The Settings class is a data class used to manage the settings of the application. It contains
 * all the settings that the user can set, such as the default reader settings, the URL of the
 * Server, and more.
 *
 * @author aless2003
 * @version 1.3.0
 * @since 1.0.0
 */
public class Settings {

  @Getter
  @JsonProperty("defaultReaderSettings")
  private final ReaderSettings defaultReaderSettings;
  @JsonProperty("mangaReaderSettings")
  private final Map<Integer, ReaderSettings> mangaReaderSettings;
  @NonNull
  @JsonProperty("url")
  private String url;
  @Getter
  @Setter
  @JsonProperty("startPopup")
  private boolean startPopup;
  @JsonProperty("defaultSearchLang")
  @Getter
  @Setter
  private String defaultSearchLang;

  @JsonProperty("defaultSourceLang")
  @Getter
  @Setter
  private String defaultSourceLang;

  @JsonCreator
  public Settings(
      @NotNull @JsonProperty("url") String url,
      @JsonProperty("startPopup") Boolean startPopup,
      @JsonProperty("defaultReaderSettings") ReaderSettings defaultReaderSettings,
      @JsonProperty("mangaReaderSettings") Map<Integer, ReaderSettings> mangaReaderSettings,
      @JsonProperty("defaultSearchLang") String defaultSearchLang,
      @JsonProperty("defaultSourceLang") String defaultSourceLang) {

    this.startPopup = Objects.requireNonNullElse(startPopup, true);

    if (defaultReaderSettings == null) {
      defaultReaderSettings = new ReaderSettings();
    }

    if (mangaReaderSettings == null) {
      mangaReaderSettings = new HashMap<>();
    }

    this.url = url;
    this.defaultReaderSettings = defaultReaderSettings;
    this.mangaReaderSettings = new HashMap<>(mangaReaderSettings);
    this.defaultSearchLang = defaultSearchLang;
    this.defaultSourceLang = defaultSourceLang;
  }

  public Settings(@NotNull String url) {
    this.url = url;
    this.defaultReaderSettings = new ReaderSettings();
    this.mangaReaderSettings = new HashMap<>();
    this.startPopup = true;
  }

  @NonNull
  public String getUrl() {
    return url;
  }

  public void setUrl(@NonNull String url) {
    this.url = url;
  }

  public ReaderSettings getReaderSettings(Integer mangaId) {

    if (mangaId == null) {
      return defaultReaderSettings;
    }

    return mangaReaderSettings.getOrDefault(mangaId, defaultReaderSettings);
  }

  /**
   * Adds {@link ReaderSettings} for a Manga based on the given Manga ID.
   *
   * @param mangaId        The ID of the Manga to add the reader settings for.
   * @param readerSettings The ReaderSettings object containing the Reader settings for the Manga.
   */
  public void addMangaReaderSettings(int mangaId, ReaderSettings readerSettings) {
    mangaReaderSettings.put(mangaId, readerSettings);
  }

  /**
   * Checks if the manga reader has {@link ReaderSettings} for the given manga ID.
   *
   * @param mangaId The ID of the manga to check for settings.
   * @return {@code true} if the manga reader has settings for the given manga ID, {@code false}
   * otherwise.
   */
  public boolean hasMangaReaderSettings(int mangaId) {
    return mangaReaderSettings.containsKey(mangaId);
  }

  /**
   * Checks if the User has a default search language set.
   *
   * @return {@code true} if the manga reader has a default search language set, {@code false}
   * otherwise.
   */
  public boolean hasDefaultSearchLang() {
    return defaultSearchLang != null && !defaultSearchLang.isEmpty();
  }
}
