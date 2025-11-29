package lk.ijse.etecmanagementsystem.dto;

import lk.ijse.etecmanagementsystem.util.ProductCondition;

public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private double sellPrice;
    private String category;
    private ProductCondition condition;
    private String imagePath;
    private double buyPrice;
    private int warrantyMonth;
    private int qty;

    public ProductDTO(String name, double price, String category, String imagePath) {
        this.name = name;
        this.sellPrice = price;
        this.category = category;
        this.imagePath = imagePath;
    }

    public ProductDTO(String id, String name, String category, double sellPrice, int warrantyMonth, int qty) {
        this.id = id;
        this.name = name;
        this.sellPrice = sellPrice;
        this.category = category;
        this.warrantyMonth = warrantyMonth;
        this.qty = qty;
    }

    public ProductDTO(String id, String name, double sellPrice, String category, String imagePath, double buyPrice, int warrantyMonth,ProductCondition condition, int qty) {
        this.id = id;
        this.name = name;
        this.sellPrice = sellPrice;
        this.category = category;
        this.imagePath = imagePath;
        this.buyPrice = buyPrice;
        this.warrantyMonth = warrantyMonth;
        this.condition = condition;
        this.qty = qty;
    }
    public ProductDTO(String id, String name, String description, double sellPrice, String category, String imagePath, double buyPrice, int warrantyMonth,ProductCondition condition, int qty) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sellPrice = sellPrice;
        this.category = category;
        this.imagePath = imagePath;
        this.buyPrice = buyPrice;
        this.warrantyMonth = warrantyMonth;
        this.condition = condition;
        this.qty = qty;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return sellPrice;
    }

    public String getCategory() {
        return category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public int getWarrantyMonth() {
        return warrantyMonth;
    }

    public int getQty() {
        return qty;
    }

    public  void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setWarrantyMonth(int warrantyMonth) {
        this.warrantyMonth = warrantyMonth;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public ProductCondition getCondition() {
        return condition;
    }

    public void setCondition(ProductCondition condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "name='" + name + '\'' +
                ", sellPrice=" + sellPrice +
                ", category='" + category + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", buyPrice=" + buyPrice +
                ", warrantyMonth=" + warrantyMonth +
                ", qty=" + qty +
                '}';
    }
}