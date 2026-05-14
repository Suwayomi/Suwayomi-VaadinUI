/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.scroller.source;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.component.scroller.EndScroller;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga;
import online.hatsunemiku.tachideskvaadinui.exceptions.CloudflareException;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SourceExploreScroller extends EndScroller {

    private final SourceService sourceService;
    private int currentPage;
    @Getter
    private final ExploreType type;
    private final String sourceId;
    private final Div content = new Div();
    private final SettingsService settingsService;
    private final ExecutorService pageLoader = Executors.newFixedThreadPool(1);

    public SourceExploreScroller(
            SourceService sourceService,
            ExploreType type,
            String sourceId,
            SettingsService settingsService) {
        super();
        this.sourceService = sourceService;
        this.settingsService = settingsService;
        this.currentPage = 1;
        this.type = type;
        this.sourceId = sourceId;

        setClassName("explore-scroller");

        content.setClassName("explore-scroller-manga-grid");
        try {
            loadNextPage();
        } catch (CloudflareException e) {
            var ui = getUI().orElse(UI.getCurrent());
            ui.access(() -> {
                Notification notification = new Notification(e.getMessage());
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(10000);
                notification.open();
            });
        }

        setContent(content);

        addScrollToEndListener(
                e -> {
                    ThreadPoolExecutor executor = (ThreadPoolExecutor) pageLoader;
                    if (executor.getActiveCount() > 0) {
                        return;
                    }
                    pageLoader.submit(this::loadNextPage);
                });
    }

    /**
     * Loads the next Page of Manga and adds it to the UI.
     */
    private void loadNextPage() {
        List<Manga> manga;
        try {
            manga = switch (type) {
                case POPULAR -> loadPopularPage();
                case LATEST -> loadLatestPage();
            };
        } catch (CloudflareException e) {
            throw e;
        } catch (RuntimeException e) {
            return;
        }

        if (manga.isEmpty()) {
            return;
        }

        currentPage++;

        Settings settings = settingsService.getSettings();

        var optUi = getUI();

        UI ui;

        if (optUi.isEmpty()) {
            if (UI.getCurrent() == null) {
                return;
            }

            ui = UI.getCurrent();
        } else {
            ui = optUi.get();
        }

        for (Manga m : manga) {
            ui.access(
                    () -> {
                        MangaCard card = new MangaCard(settings, m);
                        content.add(card);
                    });
        }
    }

    private List<Manga> loadPopularPage() {
        return sourceService.getPopularManga(sourceId, currentPage).getMangaList();
    }

    private List<Manga> loadLatestPage() {
        return sourceService.getLatestManga(sourceId, currentPage).getMangaList();
    }
}
