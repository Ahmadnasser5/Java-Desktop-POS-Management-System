package com.pos.product.model;

import javafx.beans.property.*;

public class Product {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty barcode = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty stockQuantity = new SimpleIntegerProperty();
    private final IntegerProperty minStockLevel = new SimpleIntegerProperty();
    private final IntegerProperty categoryId = new SimpleIntegerProperty();
    private final StringProperty categoryName = new SimpleStringProperty();

    public Product() {
    }

    public Product(int id, String name, String barcode, double price, int stockQuantity,
                    int minStockLevel, int categoryId, String categoryName) {
        setId(id);
        setName(name);
        setBarcode(barcode);
        setPrice(price);
        setStockQuantity(stockQuantity);
        setMinStockLevel(minStockLevel);
        setCategoryId(categoryId);
        setCategoryName(categoryName);
    }

    // --- id ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // --- name ---
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    // --- barcode ---
    public String getBarcode() { return barcode.get(); }
    public void setBarcode(String value) { barcode.set(value); }
    public StringProperty barcodeProperty() { return barcode; }

    // --- price ---
    public double getPrice() { return price.get(); }
    public void setPrice(double value) { price.set(value); }
    public DoubleProperty priceProperty() { return price; }

    // --- stockQuantity ---
    public int getStockQuantity() { return stockQuantity.get(); }
    public void setStockQuantity(int value) { stockQuantity.set(value); }
    public IntegerProperty stockQuantityProperty() { return stockQuantity; }

    // --- minStockLevel ---
    public int getMinStockLevel() { return minStockLevel.get(); }
    public void setMinStockLevel(int value) { minStockLevel.set(value); }
    public IntegerProperty minStockLevelProperty() { return minStockLevel; }

    // --- categoryId ---
    public int getCategoryId() { return categoryId.get(); }
    public void setCategoryId(int value) { categoryId.set(value); }
    public IntegerProperty categoryIdProperty() { return categoryId; }

    // --- categoryName (joined for display) ---
    public String getCategoryName() { return categoryName.get(); }
    public void setCategoryName(String value) { categoryName.set(value); }
    public StringProperty categoryNameProperty() { return categoryName; }

    /** True once stock_quantity drops to or below min_stock_level - drives the low-stock alert/highlight. */
    public boolean isLowStock() {
        return getStockQuantity() <= getMinStockLevel();
    }
}
