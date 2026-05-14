/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.items;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;

@CssImport("./css/components/items/source-item.css")
public class SourceItem extends Div {

  public SourceItem(Source source, Settings settings) {
    setClassName("extension-card"); // Use the modern card style directly on the component

    Div header = new Div();
    header.setClassName("card-header");

    Image icon = new Image();
    icon.setClassName("card-icon");
    String baseUrl = settings == null ? "" : settings.getUrl();
    String iconUrl = source.getIconUrl() == null ? "" : source.getIconUrl();
    icon.setSrc(baseUrl + iconUrl);
    icon.setAlt(source.getDisplayName());

    Div titleWrap = new Div();
    titleWrap.setClassName("card-title-wrap");
    Span name = new Span(source.getDisplayName());
    name.setClassName("card-name");
    
    Span meta = new Span("Source ID: " + source.getId());
    meta.setClassName("card-meta");
    titleWrap.add(name, meta);

    Span langBadge = new Span((source.getLang() == null ? "--" : source.getLang()).toUpperCase());
    langBadge.setClassName("lang-badge");
    header.add(icon, titleWrap, langBadge);

    Div footer = new Div();
    footer.setClassName("card-footer");
    
    Button exploreBtn = new Button("Explore");
    exploreBtn.setClassName("card-primary-btn");
    exploreBtn.addClickListener(
        e -> {
          var possibleUi = getUI();
          if (possibleUi.isPresent()) {
            var ui = possibleUi.get();
            ui.navigate("source/explore/" + source.getId());
          }
        });

    footer.add(exploreBtn);
    add(header, footer);
  }
}
