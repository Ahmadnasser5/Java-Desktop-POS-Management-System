package com.pos.product.controller;

import com.pos.product.dao.CategoryDAO;
import com.pos.product.dao.ProductDAO;
import com.pos.product.model.Category;
import com.pos.product.model.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class ProductController {

    // --- شريط الأدوات ---
    @FXML private TextField searchField;
    @FXML private CheckBox lowStockOnlyCheckBox;
    @FXML private Label lowStockCountLabel;

    // --- الجدول ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colBarcode;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStockQuantity;
    @FXML private TableColumn<Product, Integer> colMinStockLevel;

    // --- الفورم ---
    @FXML private TextField barcodeField;
    @FXML private TextField nameField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextField stockQuantityField;
    @FXML private TextField minStockLevelField;
    @FXML private Label statusLabel;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private final ObservableList<Product> productData = FXCollections.observableArrayList();
    private Product selectedProduct;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colBarcode.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("barcode"));
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("categoryName"));
        colPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));
        colStockQuantity.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stockQuantity"));
        colMinStockLevel.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("minStockLevel"));

        productTable.setItems(productData);

        // تلوين صفوف المخزون المنخفض عبر كلاس CSS "low-stock-row" (راجع styles.css)
        productTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("low-stock-row");
                if (item != null && !empty && item.isLowStock()) {
                    getStyleClass().add("low-stock-row");
                }
            }
        });

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });

        loadCategories();
        handleRefresh();
    }

    /** يفتح شاشة إدارة التصنيفات في نافذة منفصلة، ويحدّث قائمة الفئات هنا بعد إغلاقها. */
    @FXML
    private void handleManageCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryDialog.fxml"));
            Parent root = loader.load();

            CategoryDialogController dialogController = loader.getController();
            dialogController.setOnCategoriesChanged(this::loadCategories);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("إدارة التصنيفات");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(productTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // في حال قفل المستخدم النافذة من زر الإغلاق (X) بدل زر "إغلاق"
            loadCategories();
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("تعذّر فتح شاشة إدارة التصنيفات.");
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void handleRefresh() {
        List<Product> products;
        if (lowStockOnlyCheckBox.isSelected()) {
            products = productDAO.getLowStockProducts();
        } else {
            products = productDAO.getAllProducts();
        }
        productData.setAll(products);
        updateLowStockCount();
        setStatus("تم تحميل " + products.size() + " منتج.");
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            handleRefresh();
            return;
        }
        productData.setAll(productDAO.searchProducts(keyword));
        setStatus("تم العثور على " + productData.size() + " نتيجة لـ \"" + keyword + "\".");
    }

    /** يفتح نافذة منفصلة تعرض كل منتج وصل لكميته الدنيا أو أقل. */
    @FXML
    private void handleShowLowStockReport() {
        List<Product> lowStock = productDAO.getLowStockProducts();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("تقرير المخزون المنخفض");
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        alert.setHeaderText(lowStock.isEmpty()
                ? "لا يوجد أي منتج ناقص حاليًا."
                : "يوجد " + lowStock.size() + " منتج يحتاج إعادة تخزين:");

        if (!lowStock.isEmpty()) {
            String body = lowStock.stream()
                    .map(p -> String.format("• %s (الباركود: %s) — متبقي %d، الحد الأدنى %d",
                            p.getName(),
                            p.getBarcode() == null ? "—" : p.getBarcode(),
                            p.getStockQuantity(),
                            p.getMinStockLevel()))
                    .collect(Collectors.joining("\n"));
            alert.setContentText(body);
        }
        alert.showAndWait();
    }

    @FXML
    private void handleAdd() {
        Product p = readForm(null);
        if (p == null) return;

        if (productDAO.addProduct(p)) {
            setStatus("تم إضافة المنتج بنجاح.");
            handleClear();
            handleRefresh();
        } else {
            setStatus("فشل إضافة المنتج. تأكد أن الباركود غير مكرر.");
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedProduct == null) {
            setStatus("اختر منتجًا من الجدول أولاً.");
            return;
        }
        Product p = readForm(selectedProduct.getId());
        if (p == null) return;

        if (productDAO.updateProduct(p)) {
            setStatus("تم تحديث المنتج بنجاح.");
            handleClear();
            handleRefresh();
        } else {
            setStatus("فشل تحديث المنتج.");
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedProduct == null) {
            setStatus("اختر منتجًا من الجدول أولاً.");
            return;
        }
        if (productDAO.deleteProduct(selectedProduct.getId())) {
            setStatus("تم حذف المنتج.");
            handleClear();
            handleRefresh();
        } else {
            setStatus("فشل حذف المنتج.");
        }
    }

    @FXML
    private void handleClear() {
        selectedProduct = null;
        barcodeField.clear();
        nameField.clear();
        priceField.clear();
        stockQuantityField.clear();
        minStockLevelField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        productTable.getSelectionModel().clearSelection();
    }

    // --- دوال مساعدة ---

    private void populateForm(Product p) {
        selectedProduct = p;
        barcodeField.setText(p.getBarcode());
        nameField.setText(p.getName());
        priceField.setText(String.valueOf(p.getPrice()));
        stockQuantityField.setText(String.valueOf(p.getStockQuantity()));
        minStockLevelField.setText(String.valueOf(p.getMinStockLevel()));

        categoryComboBox.getItems().stream()
                .filter(c -> c.getId() == p.getCategoryId())
                .findFirst()
                .ifPresent(c -> categoryComboBox.getSelectionModel().select(c));
    }

    /** يتحقق من صحة الفورم ويبني Product. يرجع null (ويحدّث رسالة الحالة) لو فيه خطأ. */
    private Product readForm(Integer existingId) {
        String barcode = barcodeField.getText().trim();
        String name = nameField.getText().trim();
        Category category = categoryComboBox.getSelectionModel().getSelectedItem();

        if (name.isEmpty()) {
            setStatus("اسم المنتج مطلوب.");
            return null;
        }

        double price;
        int stockQuantity;
        int minStockLevel;
        try {
            price = Double.parseDouble(priceField.getText().trim());
            stockQuantity = Integer.parseInt(stockQuantityField.getText().trim());
            minStockLevel = minStockLevelField.getText().trim().isEmpty()
                    ? 5
                    : Integer.parseInt(minStockLevelField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("يجب أن يكون السعر والكمية والحد الأدنى أرقامًا.");
            return null;
        }

        Product p = new Product();
        if (existingId != null) {
            p.setId(existingId);
        }
        p.setBarcode(barcode.isEmpty() ? null : barcode);
        p.setName(name);
        p.setPrice(price);
        p.setStockQuantity(stockQuantity);
        p.setMinStockLevel(minStockLevel);
        p.setCategoryId(category != null ? category.getId() : 0);
        return p;
    }

    private void updateLowStockCount() {
        long lowStockCount = productDAO.getLowStockProducts().size();
        boolean hasLowStock = lowStockCount > 0;
        lowStockCountLabel.setText(hasLowStock ? lowStockCount + " منتج بمخزون منخفض" : "");
        lowStockCountLabel.setVisible(hasLowStock);
        lowStockCountLabel.setManaged(hasLowStock);
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
