package online.hatsunemiku.tachideskvaadinui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Push
@Theme("miku")
@EnableFeignClients
@EnableAsync
@EnableCaching
@EnableScheduling
public class TachideskVaadinUiApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication app =
        new SpringApplicationBuilder(TachideskVaadinUiApplication.class).headless(false).build();

    app.run(args);
  }
}
