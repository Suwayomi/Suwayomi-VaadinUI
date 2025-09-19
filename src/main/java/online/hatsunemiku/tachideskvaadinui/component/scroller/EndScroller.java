/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.scroller;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.shared.Registration;
import org.intellij.lang.annotations.Language;

public class EndScroller extends Scroller {
  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    @Language("JavaScript")
    String scrollToEndCheck =
        """
        var self = this;
        this.addEventListener("scroll", function(event) {
          if (self.scrollTop + self.clientHeight > (self.scrollHeight - self.clientHeight/4)) {
            var e = new Event("scroll-to-end");
            self.dispatchEvent(e);
          }
        })
        """;

    getElement().executeJs(scrollToEndCheck);
  }

  public Registration addScrollToEndListener(ComponentEventListener<ScrollToEndEvent> listener) {
    return addListener(ScrollToEndEvent.class, listener);
  }

  @DomEvent("scroll-to-end")
  public static class ScrollToEndEvent extends ComponentEvent<Scroller> {
    public ScrollToEndEvent(Scroller source, boolean isFromClient) {
      super(source, isFromClient);
    }
  }
}
