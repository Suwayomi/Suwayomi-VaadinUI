package online.hatsunemiku.tachideskvaadinui.services;

import java.net.URI;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SearchQueryParameters;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SearchResponse;
import online.hatsunemiku.tachideskvaadinui.services.client.SearchClient;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

  private final SettingsService settingsService;
  private final SearchClient searchClient;

  public SearchService(SearchClient searchClient, SettingsService settingsService) {
    this.searchClient = searchClient;
    this.settingsService = settingsService;
  }

  public SearchResponse search(String query, long sourceId, int pageNum) {

    Settings settings = settingsService.getSettings();

    URI baseUrl = URI.create(settings.getUrl());

    SearchQueryParameters searchQueryParameters =
        SearchQueryParameters.builder().pageNum(pageNum).searchTerm(query).build();

    try {
      return searchClient.search(baseUrl, sourceId, searchQueryParameters);
    } catch (Exception e) {
      return new SearchResponse(List.of(), false);
    }
  }
}
