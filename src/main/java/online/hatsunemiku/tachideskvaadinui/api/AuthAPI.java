/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.api;

import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.tracking.OAuthResponse;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.MyAnimeListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.tracker.SuwayomiTrackingService;
import org.intellij.lang.annotations.Language;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("validate")
@Slf4j
public class AuthAPI {

  private final TrackingDataService dataService;
  private final SuwayomiTrackingService suwayomiTrackingService;
  private final MyAnimeListAPIService malAPI;

  public AuthAPI(TrackingDataService dataService, SuwayomiTrackingService suwayomiTrackingService, MyAnimeListAPIService malAPI) {
    this.dataService = dataService;
    this.suwayomiTrackingService = suwayomiTrackingService;
    this.malAPI = malAPI;
  }

  /**
   * Retrieves the token from the URL hash, decodes it, and sends a POST request to the
   * "/validate/anilist" endpoint with the token for validation. Redirects the user to the response
   * URL.
   *
   * @return the HTML content with the JavaScript code to execute the data extraction and redirect
   *     to the response URL
   */
  @GetMapping(value = "anilist", produces = MediaType.TEXT_HTML_VALUE)
  public String validateAniListToken() {
    @Language("JavaScript")
    String jsToExecute =
        """
        var hash = window.location.hash.substring(1);

        // Split the hash by & to get an array of key-value pairs
        var pairs = hash.split("&");

        // Create an empty object to store the fragments
        var fragments = {};

        // Loop through the pairs and assign them to the object
        for (var i = 0; i < pairs.length; i++) {
          // Split each pair by = to get the key and value
          var pair = pairs[i].split("=");
          // Decode the key and value and assign them to the object
          fragments[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
        }

        console.log(fragments);

        var body = JSON.stringify(fragments);

        console.log(body);

        fetch('/validate/anilist', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: body
        }).then(response => {
          console.log(response);
          window.location.href = response.url;
        })
        .catch((error) => {
          console.error('Error:', error);
        });

        """;

    @Language("HTML")
    String html =
        """
        <script>
          document.addEventListener("DOMContentLoaded", function() {
            %s
          });
        </script>
        """
            .formatted(jsToExecute);

    return html;
  }

  @GetMapping("suwayomi")
  public RedirectView authenticateSuwayomi(HttpServletRequest request, @RequestParam("state") String json) {
    String url = request.getRequestURL() + "?" + request.getQueryString();

    JsonObject state = Json.parse(json);

    int trackerId = (int) state.getNumber("trackerId");

    suwayomiTrackingService.loginSuwayomi(url, trackerId);

    return new RedirectView("/");
  }


  /**
   * Validates the AniList token received in the request body. If the token is valid, it is saved in
   * the application settings. Otherwise, redirects the user to the homepage.
   *
   * @param response the OAuthResponse object containing the access token to be validated
   * @return the RedirectView object to redirect the user to the appropriate page
   */
  @PostMapping("anilist")
  public RedirectView redirectAniListToken(@RequestBody OAuthResponse response) {
    log.info("Validating AniList token");

    if (response.getAccessToken() == null || response.getAccessToken().isEmpty()) {
      log.info("AniList token is empty");

      return new RedirectView("/");
    }

    if (response.getAccessToken().length() < 740) {
      log.info("AniList token is too short");
      return new RedirectView("/");
    }

    dataService.getTokens().setAniListAuth(response);
    return new RedirectView("/");
  }

  //mal = http://localhost:8080/validate/mal?code={code}
  @GetMapping("mal")
  public RedirectView validateMALToken(@RequestParam("code") String code, @RequestParam("state") MALTokenState state) {
    log.info("Validating MAL token");

    log.info("Code: {}", code);
    log.info("state: {}", state);

    malAPI.exchangeCodeForTokens(code, state.pkceId());

    return new RedirectView("/");
  }

  public record MALTokenState(String pkceId) {

  }

}
