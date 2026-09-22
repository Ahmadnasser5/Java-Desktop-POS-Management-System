package com.pos.sales;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import com.pos.product.util.ReceiptService;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import ui.components.AppButton;
import ui.components.AppTableView;
import ui.components.Toast;

public class PosController {

    private static final double TAX_RATE = 0.15;
    private static final DecimalFormat MONEY = new DecimalFormat("0.00");

    @FXML private StackPane posRootPane;
    @FXML private FlowPane categoryBar;
    @FXML private FlowPane productsGrid;
    @FXML private AppTableView<CartLine> cartTable;
    @FXML private TableColumn<CartLine, String> cartProductColumn;
    @FXML private TableColumn<CartLine, Number> cartQuantityColumn;
    @FXML private TableColumn<CartLine, Number> cartPriceColumn;
    @FXML private TableColumn<CartLine, Number> cartTotalColumn;
    @FXML private TableColumn<CartLine, CartLine> cartActionsColumn;
    @FXML private TextField productSearchField;
    @FXML private TextField discountField;
    @FXML private Label invoiceNumberLabel;
    @FXML private Label itemCountLabel;
    @FXML private Label subtotalValueLabel;
    @FXML private Label taxValueLabel;
    @FXML private Label grandTotalValueLabel;

    private final ObservableList<CartLine> cartLines = FXCollections.observableArrayList();
    private final List<PosProduct> products = List.of(
            new PosProduct(1, "تمر سكري", "التمور", 45.00),
            new PosProduct(2, "تمر خلاص", "التمور", 38.00),
            new PosProduct(3, "فستق محمص", "المكسرات", 60.00),
            new PosProduct(4, "كاجو فاخر", "المكسرات", 75.00),
            new PosProduct(5, "قهوة عربية", "القهوة", 55.00),
            new PosProduct(6, "قهوة تركية", "القهوة", 48.00),
            new PosProduct(7, "شوكولاتة مشكلة", "حلويات", 32.00),
            new PosProduct(8, "بسكويت بالتمر", "حلويات", 22.00),
            new PosProduct(9, "مياه معدنية", "متنوع", 10.00),
            new PosProduct(10, "عصير برتقال", "متنوع", 18.00)
    );

    @FXML
    private void initialize() {
        invoiceNumberLabel.setText("فاتورة #" + (System.currentTimeMillis() % 100000));
        configureCartTable();
        cartTable.setItems(cartLines);
        buildCategories();
        productSearchField.textProperty().addListener((observable, oldValue, newValue) ->
                showProducts("الكل", newValue));
        discountField.textProperty().addListener((observable, oldValue, newValue) -> updateTotals());
        showProducts("الكل", "");
        updateTotals();
    }

    private void configureCartTable() {
        cartProductColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().product().name()));
        cartQuantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().quantity()));
        cartPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().product().price()));
        cartTotalColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().lineTotal()));
        cartActionsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        cartActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final AppButton removeButton = new AppButton("حذف", AppButton.Variant.DANGER);

            {
                removeButton.getStyleClass().add("table-action-button");
                removeButton.setOnAction(event -> {
                    CartLine line = getTableView().getItems().get(getIndex());
                    cartLines.remove(line);
                    updateTotals();
                });
            }

            @Override
            protected void updateItem(CartLine line, boolean empty) {
                super.updateItem(line, empty);
                setGraphic(empty || line == null ? null : removeButton);
            }
        });
    }

    private void buildCategories() {
        categoryBar.getChildren().clear();
        for (String category : List.of("الكل", "التمور", "المكسرات", "القهوة", "متنوع", "حلويات")) {
            AppButton categoryButton = new AppButton(category, AppButton.Variant.SECONDARY);
            categoryButton.getStyleClass().addAll("pos-category-button", categoryStyleClass(category));
            categoryButton.setOnAction(event -> {
                categoryBar.getChildren().forEach(node -> node.getStyleClass().remove("pos-category-selected"));
                categoryButton.getStyleClass().add("pos-category-selected");
                showProducts(category, productSearchField.getText());
            });
            categoryBar.getChildren().add(categoryButton);
        }
        categoryBar.getChildren().get(0).getStyleClass().add("pos-category-selected");
    }

    private String categoryStyleClass(String category) {
        return switch (category) {
            case "التمور" -> "pos-category-dates";
            case "المكسرات" -> "pos-category-nuts";
            case "القهوة" -> "pos-category-coffee";
            case "متنوع" -> "pos-category-misc";
            case "حلويات" -> "pos-category-sweets";
            default -> "pos-category-all";
        };
    }

    private void showProducts(String category, String searchTerm) {
        productsGrid.getChildren().clear();
        String normalizedSearch = searchTerm == null ? "" : searchTerm.trim().toLowerCase(Locale.ROOT);
        products.stream()
                .filter(product -> "الكل".equals(category) || product.category().equals(category))
                .filter(product -> normalizedSearch.isEmpty()
                        || product.name().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .forEach(product -> productsGrid.getChildren().add(productButton(product)));
    }

    private Button productButton(PosProduct product) {
        AppButton button = new AppButton(product.name() + "\n" + MONEY.format(product.price()),
                AppButton.Variant.SECONDARY);
        button.getStyleClass().add("pos-product-button");
        button.setWrapText(true);
        button.setOnAction(event -> addToCart(product));
        return button;
    }

    private void addToCart(PosProduct product) {
        cartLines.stream()
                .filter(line -> line.product().id() == product.id())
                .findFirst()
                .ifPresentOrElse(CartLine::increase, () -> cartLines.add(new CartLine(product)));
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cartLines.stream().mapToDouble(CartLine::lineTotal).sum();
        double tax = subtotal * TAX_RATE;
        double discount = readDiscount();
        double grandTotal = Math.max(0, subtotal + tax - discount);
        int itemCount = cartLines.stream().mapToInt(CartLine::quantity).sum();
        subtotalValueLabel.setText(MONEY.format(subtotal));
        taxValueLabel.setText(MONEY.format(tax));
        grandTotalValueLabel.setText(MONEY.format(grandTotal));
        itemCountLabel.setText(itemCount + " صنف");
    }

    private double readDiscount() {
        try {
            return Math.max(0, Double.parseDouble(discountField.getText().trim()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    @FXML
    private void onNewInvoice() {
        cartLines.clear();
        discountField.clear();
        invoiceNumberLabel.setText("فاتورة #" + (System.currentTimeMillis() % 100000));
        updateTotals();
        Toast.info(posRootPane, "تم فتح فاتورة جديدة.");
    }

    @FXML
    private void onDeleteSelectedItem() {
        CartLine selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Toast.info(posRootPane, "اختر صنفًا من السلة أولاً.");
            return;
        }
        cartLines.remove(selected);
        updateTotals();
    }

    @FXML
    private void handleCheckout() {
        if (cartLines.isEmpty()) {
            Toast.error(posRootPane, "السلة فارغة.");
            return;
        }

        double subtotal = cartLines.stream().mapToDouble(CartLine::lineTotal).sum();
        double discount = readDiscount();
        double tax = subtotal * TAX_RATE;
        String receiptNo = invoiceNumberLabel.getText().replace("فاتورة #", "");
        ReceiptService receiptService = new ReceiptService(cartLines, receiptNo, "الكاشير", discount, tax);

        if (!receiptService.print()) {
            Toast.error(posRootPane, "تعذر طباعة الفاتورة.");
            return;
        }

        Toast.success(posRootPane, "تم تسجيل المعاملة الحالية بإجمالي " + grandTotalValueLabel.getText() + " ريال.");
        cartLines.clear();
        discountField.clear();
        updateTotals();
    }

    @FXML
    private void onPlaceholderAction() {
        Toast.info(posRootPane, "هذه العملية ستتصل بسجل الفواتير عند ربط قاعدة البيانات.");
    }

    public record PosProduct(int id, String name, String category, double price) {
    }

    public static final class CartLine {
        private final PosProduct product;
        private int quantity = 1;

        private CartLine(PosProduct product) {
            this.product = product;
        }

        public PosProduct product() {
            return product;
        }

        public int quantity() {
            return quantity;
        }

        public double lineTotal() {
            return product.price() * quantity;
        }

        private void increase() {
            quantity++;
        }
    }
}