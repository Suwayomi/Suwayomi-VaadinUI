/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.*;
import jakarta.servlet.http.HttpServletResponse;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

/**
 * The {@code InternalErrorView} class provides a high-tech, Hatsune Miku themed error page
 * matching the Stitch design. It extends {@link StandardLayout} to maintain design consistency
 * with the rest of the application.
 */
@CssImport("./css/views/internal-error.css")
@Route("error")
public class InternalErrorView extends StandardLayout implements HasErrorParameter<Exception> {

    private final Span errorCodeBadge;
    private final H2 errorTitle;
    private final Span errorMessage;

    public InternalErrorView() {
        super("Error");
        addClassName("internal-error-view");

        // Content
        Div errorContent = new Div();
        errorContent.setClassName("error-content");

        Div glitchOverlay = new Div();
        glitchOverlay.setClassName("glitch-overlay");
        errorContent.add(glitchOverlay);

        Div errorCard = new Div();
        errorCard.setClassName("error-card");

        Div iconWrapper = new Div();
        iconWrapper.setClassName("status-icon-wrapper");
        iconWrapper.add(VaadinIcon.REFRESH.create()); // Sync arrows
        iconWrapper.getChildren().forEach(c -> c.setClassName("status-icon"));

        errorTitle = new H2("Page Load Failed");
        errorTitle.setClassName("error-title");

        errorCodeBadge = new Span("ERROR CODE: SCR_SYNC_TIMEOUT");
        errorCodeBadge.setClassName("error-badge");

        errorMessage = new Span("The manga synchronization server timed out due to heavy traffic or network disruption.");
        errorMessage.setClassName("error-description");

        Div buttonGroup = new Div();
        buttonGroup.setClassName("button-group");

        Div libButton = new Div();
        libButton.setClassName("stitch-btn btn-primary");
        libButton.add(VaadinIcon.GRID_BIG_O.create());
        libButton.add(new Span("Back to Library"));
        libButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(RootView.class)));

        Div settingsButton = new Div();
        settingsButton.setClassName("stitch-btn btn-secondary");
        settingsButton.add(VaadinIcon.COG.create());
        settingsButton.add(new Span("Settings"));
        settingsButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(SettingsView.class)));

        buttonGroup.add(libButton, settingsButton);

        errorCard.add(iconWrapper, errorTitle, errorCodeBadge, errorMessage, buttonGroup);
        errorContent.add(errorCard);

        // Footer Info (Specific to error page)
        Div footerInfo = new Div();
        footerInfo.setText("VaaUI Error Handler");
        footerInfo.setClassName("error-footer-info");
        errorContent.add(footerInfo);

        setContent(errorContent);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        if (parameter.getException() instanceof NotFoundException) {
            statusCode = HttpServletResponse.SC_NOT_FOUND;
            errorTitle.setText("Resource Not Found");
            errorCodeBadge.setText("ERROR CODE: 404_NOT_FOUND");
            errorMessage.setText("The requested digital resource does not exist.");
        } else {
            errorTitle.setText("Page Load Failed");
            errorCodeBadge.setText("ERROR CODE: 500_INTERNAL_GLITCH");
            
            String details = parameter.getException() != null ? parameter.getException().getMessage() : "Unknown sync error";
            errorMessage.setText("The server encountered an unhandled exception: " + details);
        }

        return statusCode;
    }
}
