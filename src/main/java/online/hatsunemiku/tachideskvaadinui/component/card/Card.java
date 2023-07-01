package online.hatsunemiku.tachideskvaadinui.component.card;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CssImport("css/card.css")
public class Card extends Div {

  private static final Logger logger = LoggerFactory.getLogger(Card.class);

  public Card(String title, String imageUrl) {
    setClassName("card");
    addClassName("shadow-m");
    addClassName("border");

    Image img = new Image(imageUrl, "Thumbnail");

    Paragraph p = new Paragraph(title);
    p.addClassName("card-title");

    add(img, p);

    addClickListener(e -> logger.info("Clicked on card"));
  }
}
