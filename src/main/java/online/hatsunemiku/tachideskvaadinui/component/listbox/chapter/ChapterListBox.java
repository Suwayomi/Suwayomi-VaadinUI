/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.listbox.chapter;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.listbox.ListBox;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;

@CssImport("./css/components/chapter-list-box.css")
public class ChapterListBox extends ListBox<Chapter> {

  public ChapterListBox(List<Chapter> chapters, MangaService mangaService) {
    super();
    setItems(chapters);
    setRenderer(new ChapterRenderer(mangaService));
  }
}
