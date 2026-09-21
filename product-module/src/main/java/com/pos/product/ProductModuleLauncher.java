package com.pos.product;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Standalone entry point - only used to test this module on its own,
 * BEFORE it's wired into the main project's navigation/menu.
 * Delete this class once the ProductView screen is opened from your
 * main application instead (e.g. from a sidebar menu item).
 */
public class ProductModuleLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/ProductView.fxml"));
        stage.setTitle("Products & Inventory - Standalone Test");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
