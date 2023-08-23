package online.hatsunemiku.tachideskvaadinui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Push
@Theme("miku")
@EnableFeignClients
@EnableAsync
public class TachideskVaadinUiApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplicationBuilder(TachideskVaadinUiApplication.class)
        .headless(false)
        .build();

    app.run(args);
  }
}
