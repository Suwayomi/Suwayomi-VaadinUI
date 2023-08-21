package online.hatsunemiku.tachideskvaadinui.api;

import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.services.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("validate")
@Slf4j
public class AuthAPI {

  private final SettingsService settingsService;
  private final AniListAPIService aniListAPIService;

  public AuthAPI(SettingsService settingsService, AniListAPIService aniListAPIService) {
    this.settingsService = settingsService;
    this.aniListAPIService = aniListAPIService;
  }

  @RequestMapping("anilist")
  public RedirectView validateAniListToken(@RequestParam String code) {
    log.info("Validating AniList token");

    if (code == null || code.isEmpty()) {
      log.info("AniList token is empty");

      return new RedirectView("/");
    }

    if (code.length() < 740) {
      log.info("AniList token is too short");
      return new RedirectView("/");
    }

    try {
      aniListAPIService.requestOAuthToken(code);
    } catch (Exception e) {
      log.error("Error while getting AniList token", e);
    }

    return new RedirectView("/");
  }

}
