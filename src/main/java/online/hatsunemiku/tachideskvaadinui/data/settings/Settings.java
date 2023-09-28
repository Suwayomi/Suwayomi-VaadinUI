package online.hatsunemiku.tachideskvaadinui.data.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

public class Settings {

  @NonNull
  @JsonProperty("url")
  private String url;
  @JsonProperty("defaultReaderSettings")
  private final ReaderSettings defaultReaderSettings;
  @JsonProperty("mangaReaderSettings")
  private final Map<Integer, ReaderSettings> mangaReaderSettings;

  @JsonCreator
  public Settings(@NotNull @JsonProperty("url") String url,
      @JsonProperty("readerSettings") ReaderSettings defaultReaderSettings,
      @JsonProperty("mangaReaderSettings") Map<Integer, ReaderSettings> mangaReaderSettings) {

    if (defaultReaderSettings == null) {
      defaultReaderSettings = new ReaderSettings();
    }

    if (mangaReaderSettings == null) {
      mangaReaderSettings = new HashMap<>();
    }

    this.url = url;
    this.defaultReaderSettings = defaultReaderSettings;
    this.mangaReaderSettings = mangaReaderSettings;
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

  public void addMangaReaderSettings(int mangaId, ReaderSettings readerSettings) {
    mangaReaderSettings.put(mangaId, readerSettings);
  }
}
