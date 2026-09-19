package com.pos.product.controller;

import com.pos.product.dao.CategoryDAO;
import com.pos.product.dao.ProductDAO;
import com.pos.product.model.Category;
import com.pos.product.model.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class CategoryDialogController {

    @FXML private ListView<Category> categoryListView;
    @FXML private TextField newCategoryField;
    @FXML private Label categoryStatusLabel;

    @FXML private Label productsSectionLabel;
    @FXML private ListView<Product> productsInCategoryListView;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private final ObservableList<Category> categoryData = FXCollections.observableArrayList();
    private final ObservableList<Product> productsInCategoryData = FXCollections.observableArrayList();

    /** Set by ProductController so this dialog can trigger a refresh of the product screen's combo box on close. */
    private Runnable onCategoriesChanged;

    public void setOnCategoriesChanged(Runnable callback) {
        this.onCategoriesChanged = callback;
    }

    @FXML
    public void initialize() {
        categoryListView.setItems(categoryData);
        productsInCategoryListView.setItems(productsInCategoryData);

        // يعرض اسم المنتج + الكمية بدل الاعتماد على toString الافتراضي
        productsInCategoryListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText((p == null || empty) ? null : p.getName() + "  —  الكمية: " + p.getStockQuantity());
            }
        });

        // لما تدوس على تصنيف، اعرض منتجاته على طول
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            loadProductsForCategory(newSel);
        });

        loadCategories();
    }

    private void loadCategories() {
        Category previouslySelected = categoryListView.getSelectionModel().getSelectedItem();
        categoryData.setAll(categoryDAO.getAllCategories());

        // يحافظ على نفس التصنيف متحدد بعد أي تحديث (إضافة/حذف) لو لسه موجود
        if (previouslySelected != null) {
            categoryData.stream()
                    .filter(c -> c.getId() == previouslySelected.getId())
                    .findFirst()
                    .ifPresentOrElse(
                            c -> categoryListView.getSelectionModel().select(c),
                            () -> loadProductsForCategory(null));
        }
    }

    private void loadProductsForCategory(Category category) {
        if (category == null) {
            productsInCategoryData.clear();
            productsSectionLabel.setText("اختر تصنيفًا لعرض منتجاته");
            return;
        }
        List<Product> products = productDAO.getProductsByCategory(category.getId());
        productsInCategoryData.setAll(products);
        productsSectionLabel.setText("منتجات \"" + category.getName() + "\" (" + products.size() + ")");
    }

    @FXML
    private void handleAddCategory() {
        String name = newCategoryField.getText().trim();
        if (name.isEmpty()) {
            setStatus("اكتب اسم التصنيف أولاً.");
            return;
        }

        if (categoryDAO.addCategory(name)) {
            newCategoryField.clear();
            loadCategories();
            setStatus("تمت إضافة \"" + name + "\".");
        } else {
            setStatus("فشلت الإضافة. ربما يوجد تصنيف بنفس الاسم بالفعل.");
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("اختر تصنيفًا من القائمة أولاً.");
            return;
        }

        long productCount = productDAO.getProductsByCategory(selected.getId()).size();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText("حذف تصنيف \"" + selected.getName() + "\"؟");
        confirm.setContentText(productCount > 0
                ? "يوجد " + productCount + " منتج مرتبط بهذا التصنيف. لن يتم حذفهم، لكن سيصبحون بدون تصنيف."
                : "لا يوجد أي منتج مرتبط بهذا التصنيف حاليًا.");

        confirm.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
            if (categoryDAO.deleteCategory(selected.getId())) {
                loadCategories();
                setStatus("تم حذف \"" + selected.getName() + "\".");
            } else {
                setStatus("فشل حذف التصنيف.");
            }
        });
    }

    @FXML
    private void handleClose() {
        if (onCategoriesChanged != null) {
            onCategoriesChanged.run();
        }
        Stage stage = (Stage) categoryListView.getScene().getWindow();
        stage.close();
    }

    private void setStatus(String message) {
        categoryStatusLabel.setText(message);
    }
}
