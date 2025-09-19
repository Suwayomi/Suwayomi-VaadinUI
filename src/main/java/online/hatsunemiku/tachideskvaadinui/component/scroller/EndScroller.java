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
import org.jetbrains.annotations.NotNull;

/**
 * The {@code EndScroller} class is an extension of the {@link Scroller} component which provides
 * functionality to detect when the user scrolls near the bottom of the scrollable area.
 *
 * <p>This class is primarily designed to enhance scrolling behavior by enabling features such as
 * "infinite scrolling" or dynamically loading additional content as the user approaches the end of
 * the scrolling content.
 */
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

  /**
   * Adds a listener to be notified when a {@link ScrollToEndEvent} is fired. The event is triggered
   * when the user scrolls near the bottom of the scrollable content in the {@link EndScroller}
   * component.
   *
   * @param listener the listener to be notified when a {@link ScrollToEndEvent} occurs
   * @return a {@link Registration} object that can be used to remove the listener
   */
  public Registration addScrollToEndListener(ComponentEventListener<ScrollToEndEvent> listener) {
    return addListener(ScrollToEndEvent.class, listener);
  }

  /**
   * Represents a custom event fired when the user scrolls near the bottom of a {@link EndScroller}
   * component.
   *
   * <p>The event is triggered when the vertical scroll position approaches the end of the
   * scrollable area. Designed to help in implementing "infinite scrolling" functionality or
   * dynamically loading additional content.
   *
   * <p>This event can be listened for using the {@link EndScroller#addScrollToEndListener} method.
   */
  @DomEvent("scroll-to-end")
  public static class ScrollToEndEvent extends ComponentEvent<Scroller> {

    /**
     * Constructs a new {@code ScrollToEndEvent}.
     *
     * @param source the source component from which the event originated, must not be null
     * @param isFromClient {@code true} if the client triggered the event, {@code false} if
     *     triggered by the server
     */
    public ScrollToEndEvent(@NotNull Scroller source, boolean isFromClient) {
      super(source, isFromClient);
    }
  }
}
