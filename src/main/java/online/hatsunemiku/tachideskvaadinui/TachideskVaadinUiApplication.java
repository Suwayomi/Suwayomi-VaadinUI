package online.hatsunemiku.tachideskvaadinui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@Push
@Theme("miku")
@EnableFeignClients
public class TachideskVaadinUiApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(TachideskVaadinUiApplication.class, args);
  }

}
