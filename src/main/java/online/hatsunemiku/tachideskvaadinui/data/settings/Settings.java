package online.hatsunemiku.tachideskvaadinui.data.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

public class Settings {

  @NonNull
  @JsonProperty("url")
  private String url;

  @Getter
  @JsonProperty("defaultReaderSettings")
  private final ReaderSettings defaultReaderSettings;

  @JsonProperty("mangaReaderSettings")
  private final Map<Integer, ReaderSettings> mangaReaderSettings;

  @JsonCreator
  public Settings(
      @NotNull @JsonProperty("url") String url,
      @JsonProperty("defaultReaderSettings") ReaderSettings defaultReaderSettings,
      @JsonProperty("mangaReaderSettings") Map<Integer, ReaderSettings> mangaReaderSettings) {

    if (defaultReaderSettings == null) {
      defaultReaderSettings = new ReaderSettings();
    }

    if (mangaReaderSettings == null) {
      mangaReaderSettings = new HashMap<>();
    }

    this.url = url;
    this.defaultReaderSettings = defaultReaderSettings;
    this.mangaReaderSettings = new HashMap<>(mangaReaderSettings);
  }

  public Settings(@NotNull String url) {
    this.url = url;
    this.defaultReaderSettings = new ReaderSettings();
    this.mangaReaderSettings = new HashMap<>();
  }

  public void setUrl(@NonNull String url) {
    this.url = url;
  }

  @NonNull
  public String getUrl() {
    return url;
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
   * @param mangaId The ID of the Manga to add the reader settings for.
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
   *     otherwise.
   */
  public boolean hasMangaReaderSettings(int mangaId) {
    return mangaReaderSettings.containsKey(mangaId);
  }
}