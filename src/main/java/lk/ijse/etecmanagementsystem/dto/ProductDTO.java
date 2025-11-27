package lk.ijse.etecmanagementsystem.dto;

public class ProductDTO {
    private String id;
    private String name;
    private double sellPrice;
    private String category;
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