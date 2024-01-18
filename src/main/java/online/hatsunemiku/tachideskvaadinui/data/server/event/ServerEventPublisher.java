/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.server.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ServerEventPublisher {
    private final ApplicationEventPublisher publisher;

    public ServerEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishServerStartedEvent() {
        var event = new ServerStartedEvent();
        publisher.publishEvent(event);
    }
}
