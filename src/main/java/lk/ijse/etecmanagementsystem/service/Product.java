package lk.ijse.etecmanagementsystem.service;

public class Product {
    private String name;
    private double price;
    private String category;
    private String imagePath;

    public Product(String name, double price, String category, String imagePath) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
    }

    // Getters
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
}