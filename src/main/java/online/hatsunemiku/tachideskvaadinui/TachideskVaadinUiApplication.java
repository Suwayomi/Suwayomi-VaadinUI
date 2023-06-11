package online.hatsunemiku.tachideskvaadinui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Push
@Theme("miku")
public class TachideskVaadinUiApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(TachideskVaadinUiApplication.class, args);
  }

}
