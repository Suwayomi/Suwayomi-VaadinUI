/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves repository input URLs (such as GitHub repository links) to a valid extension store
 * manifest URL (e.g. raw index.min.json or index.pb).
 */
@Slf4j
@Component
public class ExtensionRepoUrlResolver {

  private static final Pattern GITHUB_BLOB_OR_RAW =
      Pattern.compile(
          "^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/(?:blob|raw)/([^/]+)/(.*)$");

  private static final Pattern GITHUB_TREE =
      Pattern.compile(
          "^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+?)(?:\\.git)?/tree/([^/]+)/?$");

  private static final Pattern GITHUB_REPO =
      Pattern.compile("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+?)(?:\\.git)?/?$");

  private static final Pattern RAW_GITHUB_FOLDER =
      Pattern.compile("^https?://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/([^/]+)/?$");

  private final Predicate<String> reachabilityChecker;
  private final HttpClient httpClient;

  public ExtensionRepoUrlResolver() {
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    this.reachabilityChecker = this::checkReachableHttp;
  }

  public ExtensionRepoUrlResolver(Predicate<String> reachabilityChecker) {
    this.httpClient = null;
    this.reachabilityChecker = reachabilityChecker;
  }

  /**
   * Generates ordered candidate index URLs for an input URL.
   *
   * @param inputUrl the user-supplied repository or manifest URL.
   * @return a list of candidate manifest URLs to probe.
   */
  public List<String> getCandidates(String inputUrl) {
    List<String> candidates = new ArrayList<>();
    if (inputUrl == null || inputUrl.isBlank()) {
      return candidates;
    }

    String trimmed = inputUrl.trim();

    // 1. Direct blob or raw github URL pointing to a file
    Matcher blobMatcher = GITHUB_BLOB_OR_RAW.matcher(trimmed);
    if (blobMatcher.matches()) {
      String owner = blobMatcher.group(1);
      String repo = blobMatcher.group(2);
      String branch = blobMatcher.group(3);
      String path = blobMatcher.group(4);
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", owner, repo, branch, path));
      return candidates;
    }

    // 2. Direct JSON or PB manifest link (already in raw or direct format)
    if (trimmed.endsWith(".json") || trimmed.endsWith(".pb")) {
      candidates.add(trimmed);
      return candidates;
    }

    // 3. GitHub tree branch URL (e.g. github.com/owner/repo/tree/branch)
    Matcher treeMatcher = GITHUB_TREE.matcher(trimmed);
    if (treeMatcher.matches()) {
      String owner = treeMatcher.group(1);
      String repo = treeMatcher.group(2);
      String branch = treeMatcher.group(3);
      candidates.add(
          String.format(
              "https://raw.githubusercontent.com/%s/%s/%s/index.min.json", owner, repo, branch));
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/%s/index.pb", owner, repo, branch));
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/%s/index.json", owner, repo, branch));
      return candidates;
    }

    // 4. Raw github folder URL (e.g. raw.githubusercontent.com/owner/repo/branch)
    Matcher rawFolderMatcher = RAW_GITHUB_FOLDER.matcher(trimmed);
    if (rawFolderMatcher.matches()) {
      String owner = rawFolderMatcher.group(1);
      String repo = rawFolderMatcher.group(2);
      String branch = rawFolderMatcher.group(3);
      candidates.add(
          String.format(
              "https://raw.githubusercontent.com/%s/%s/%s/index.min.json", owner, repo, branch));
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/%s/index.pb", owner, repo, branch));
      return candidates;
    }

    // 5. Standard GitHub repository landing URL (e.g. github.com/owner/repo)
    Matcher repoMatcher = GITHUB_REPO.matcher(trimmed);
    if (repoMatcher.matches()) {
      String owner = repoMatcher.group(1);
      String repo = repoMatcher.group(2);
      // Try 'repo' branch first (standard for Keiyoushi and Tachiyomi forks)
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/repo/index.min.json", owner, repo));
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/repo/index.pb", owner, repo));
      // Try 'main' branch
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/main/index.min.json", owner, repo));
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/main/index.pb", owner, repo));
      // Try 'master' branch
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/master/index.min.json", owner, repo));
      candidates.add(
          String.format("https://raw.githubusercontent.com/%s/%s/master/index.pb", owner, repo));
      return candidates;
    }

    // 6. Generic folder URL
    String cleanUrl = trimmed.replaceAll("/+$", "");
    candidates.add(cleanUrl + "/index.min.json");
    candidates.add(cleanUrl + "/index.pb");

    return candidates;
  }

  /**
   * Resolves the given input URL to a verified extension store manifest URL.
   *
   * @param inputUrl the user-provided URL.
   * @return an Optional containing the resolved manifest URL, or Optional.empty() if no valid
   *     candidate was found.
   */
  public Optional<String> resolve(String inputUrl) {
    if (inputUrl == null || inputUrl.isBlank()) {
      return Optional.empty();
    }

    List<String> candidates = getCandidates(inputUrl);
    for (String candidate : candidates) {
      log.debug("Checking candidate extension repo URL: {}", candidate);
      if (reachabilityChecker.test(candidate)) {
        log.info("Successfully resolved extension repo URL '{}' to '{}'", inputUrl, candidate);
        return Optional.of(candidate);
      }
    }

    // Fallback: If the user provided an explicit manifest URL (.json or .pb), allow it even if
    // reachability check failed (e.g. offline, self-hosted, or mock test environment)
    String trimmed = inputUrl.trim();
    if (!candidates.isEmpty() && (trimmed.endsWith(".json") || trimmed.endsWith(".pb"))) {
      log.info("Using explicit manifest URL '{}' without reachability confirmation", candidates.get(0));
      return Optional.of(candidates.get(0));
    }

    log.warn("Could not find any reachable extension store manifest for '{}'", inputUrl);
    return Optional.empty();
  }

  private boolean checkReachableHttp(String url) {
    try {
      HttpRequest headRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofSeconds(3))
              .header("User-Agent", "Suwayomi-VaadinUI")
              .method("HEAD", HttpRequest.BodyPublishers.noBody())
              .build();

      HttpResponse<Void> response =
          httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());
      int status = response.statusCode();
      if (status >= 200 && status < 300) {
        return true;
      }
      if (status == 405) {
        HttpRequest getRequest =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .header("User-Agent", "Suwayomi-VaadinUI")
                .header("Range", "bytes=0-10")
                .GET()
                .build();
        HttpResponse<Void> getResponse =
            httpClient.send(getRequest, HttpResponse.BodyHandlers.discarding());
        return getResponse.statusCode() >= 200 && getResponse.statusCode() < 300;
      }
      return false;
    } catch (Exception e) {
      log.debug("Candidate reachability check failed for {}: {}", url, e.getMessage());
      return false;
    }
  }
}
