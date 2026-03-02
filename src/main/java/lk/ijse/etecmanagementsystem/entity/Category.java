package lk.ijse.etecmanagementsystem.entity;

public class Category {
    String category_name;
    String description;

    public Category() {
    }

    public Category(String category_name, String description) {
        this.category_name = category_name;
        this.description = description;
    }

    public Category(String categoryName) {
        this.category_name = categoryName;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Category{" +
                "category_name='" + category_name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
