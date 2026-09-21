package com.pos;

import com.pos.core.util.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point.
 * <p>
 * Flow: start -> login scene -> (on success) main layout scene.
 * Engineers 2..5 do not need to touch this class; their screens are reached
 * through the sidebar.
 */
public class Main extends Application {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> LOG.log(Level.SEVERE, "Uncaught error", error));

        Navigator.setPrimaryStage(primaryStage);
        Navigator.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
