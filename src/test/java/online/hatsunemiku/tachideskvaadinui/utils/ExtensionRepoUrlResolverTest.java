/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExtensionRepoUrlResolverTest {

  @Test
  void testCandidateGenerationStandardGithubRepo() {
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(url -> true);

    List<String> candidates = resolver.getCandidates("https://github.com/keiyoushi/extensions");
    assertFalse(candidates.isEmpty());
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json",
        candidates.get(0));
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.pb",
        candidates.get(1));
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/main/index.min.json",
        candidates.get(2));
  }

  @Test
  void testCandidateGenerationGithubRepoWithSlashAndGit() {
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(url -> true);

    List<String> candidates = resolver.getCandidates("https://github.com/keiyoushi/extensions.git/");
    assertFalse(candidates.isEmpty());
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json",
        candidates.get(0));
  }

  @Test
  void testCandidateGenerationGithubTreeBranch() {
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(url -> true);

    List<String> candidates =
        resolver.getCandidates("https://github.com/keiyoushi/extensions/tree/mybranch");
    assertFalse(candidates.isEmpty());
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/mybranch/index.min.json",
        candidates.get(0));
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/mybranch/index.pb",
        candidates.get(1));
  }

  @Test
  void testCandidateGenerationGithubBlob() {
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(url -> true);

    List<String> candidates =
        resolver.getCandidates(
            "https://github.com/keiyoushi/extensions/blob/repo/index.min.json");
    assertEquals(1, candidates.size());
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json",
        candidates.get(0));
  }

  @Test
  void testCandidateGenerationDirectJsonUrl() {
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(url -> true);

    String directUrl = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json";
    List<String> candidates = resolver.getCandidates(directUrl);
    assertEquals(1, candidates.size());
    assertEquals(directUrl, candidates.get(0));
  }

  @Test
  void testResolveSelectsFirstReachableCandidate() {
    Set<String> reachable =
        Set.of("https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json");
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(reachable::contains);

    Optional<String> result = resolver.resolve("https://github.com/keiyoushi/extensions");
    assertTrue(result.isPresent());
    assertEquals(
        "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json",
        result.get());
  }

  @Test
  void testResolveSelectsPbCandidateIfJsonNotAvailable() {
    Set<String> reachable =
        Set.of("https://raw.githubusercontent.com/myuser/myrepo/repo/index.pb");
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(reachable::contains);

    Optional<String> result = resolver.resolve("https://github.com/myuser/myrepo");
    assertTrue(result.isPresent());
    assertEquals(
        "https://raw.githubusercontent.com/myuser/myrepo/repo/index.pb",
        result.get());
  }

  @Test
  void testResolveReturnsEmptyWhenNoCandidateReachable() {
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(url -> false);

    Optional<String> result = resolver.resolve("https://github.com/invalid/nonexistent-repo");
    assertFalse(result.isPresent());
  }

  @Test
  void testResolveAllowsExplicitManifestEvenIfReachabilityFails() {
    ExtensionRepoUrlResolver resolver = new ExtensionRepoUrlResolver(url -> false);

    String directUrl = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json";
    Optional<String> result = resolver.resolve(directUrl);
    assertTrue(result.isPresent());
    assertEquals(directUrl, result.get());
  }
}
