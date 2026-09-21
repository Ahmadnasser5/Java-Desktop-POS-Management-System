package ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * SHARED COMPONENT - short, non blocking feedback message.
 * <p>
 * The host screen must be a StackPane (the shared Content Area is one), so the
 * toast can float above the content without changing the layout.
 *
 * <pre>{@code Toast.success(contentRoot, "User saved");}</pre>
 */
public final class Toast {

    public enum Type {
        SUCCESS("toast-success"),
        ERROR("toast-error"),
        INFO("toast-info");

        private final String styleClass;

        Type(String styleClass) {
            this.styleClass = styleClass;
        }
    }

    private static final Duration FADE = Duration.millis(200);
    private static final Duration VISIBLE = Duration.seconds(3);

    private Toast() {
    }

    public static void success(StackPane host, String message) {
        show(host, message, Type.SUCCESS);
    }

    public static void error(StackPane host, String message) {
        show(host, message, Type.ERROR);
    }

    public static void info(StackPane host, String message) {
        show(host, message, Type.INFO);
    }

    public static void show(StackPane host, String message, Type type) {
        if (host == null || message == null) {
            return;
        }

        Label label = new Label(message);
        label.getStyleClass().addAll("toast", type.styleClass);
        label.setWrapText(true);
        label.setMaxWidth(420);
        label.setOpacity(0);

        StackPane.setAlignment(label, Pos.BOTTOM_CENTER);
        StackPane.setMargin(label, new Insets(0, 0, 24, 0));
        host.getChildren().add(label);

        FadeTransition in = new FadeTransition(FADE, label);
        in.setFromValue(0);
        in.setToValue(1);

        FadeTransition out = new FadeTransition(FADE, label);
        out.setFromValue(1);
        out.setToValue(0);

        SequentialTransition sequence =
                new SequentialTransition(in, new PauseTransition(VISIBLE), out);
        sequence.setOnFinished(event -> host.getChildren().remove(label));
        sequence.play();
    }
}
