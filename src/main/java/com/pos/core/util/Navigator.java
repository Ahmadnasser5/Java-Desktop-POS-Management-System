package com.pos.core.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.theme.ThemeManager;

import java.io.IOException;
import java.net.URL;

/**
 * SHARED COMPONENT - owns the primary Stage and switches between the two
 * top level scenes.
 * <p>
 * Design decision (from the Step 1 plan): ONE Stage, two Scenes.
 * The login screen is a pre-authentication scene outside the standard frame
 * (uiContract section 2 scopes the frame to post-authentication screens);
 * after a successful login the same Stage swaps to the main layout, so the
 * window icon, title and position survive.
 */
public final class Navigator {

    public static final String LOGIN_VIEW = "/fxml/auth/login-view.fxml";
    public static final String MAIN_LAYOUT = "/fxml/layout/main-layout.fxml";

    private static final double LOGIN_WIDTH = 480;
    private static final double LOGIN_HEIGHT = 560;

    /** uiContract section 3 */
    private static final double APP_WIDTH = 1280;
    private static final double APP_HEIGHT = 720;
    private static final double APP_MIN_WIDTH = 1024;
    private static final double APP_MIN_HEIGHT = 650;

    private static Stage primaryStage;

    private Navigator() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showLogin() {
        FXMLLoader fxmlLoader = loader(LOGIN_VIEW);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the view: " + LOGIN_VIEW, e);
        }

        Object controller = fxmlLoader.getController();
        if (controller instanceof com.pos.auth.controller.LoginController loginController) {
            loginController.warnIfDatabaseUnreachable();
        }

        Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);
        ThemeManager.apply(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("نظام نقاط البيع - تسجيل الدخول");
        primaryStage.setResizable(false);
        primaryStage.setMinWidth(LOGIN_WIDTH);
        primaryStage.setMinHeight(LOGIN_HEIGHT);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showMainApplication() {
        Parent root = load(MAIN_LAYOUT);
        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        ThemeManager.apply(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("نظام نقاط البيع وإدارة المبيعات");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(APP_MIN_WIDTH);
        primaryStage.setMinHeight(APP_MIN_HEIGHT);
        primaryStage.setWidth(APP_WIDTH);
        primaryStage.setHeight(APP_HEIGHT);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /** Loads an FXML file from the classpath and returns its root node. */
    public static Parent load(String resourcePath) {
        try {
            return new FXMLLoader(resource(resourcePath)).load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the view: " + resourcePath, e);
        }
    }

    /** Loads an FXML file and gives access to its controller. */
    public static FXMLLoader loader(String resourcePath) {
        return new FXMLLoader(resource(resourcePath));
    }

    private static URL resource(String resourcePath) {
        URL url = Navigator.class.getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("View not found on the classpath: " + resourcePath);
        }
        return url;
    }
}
