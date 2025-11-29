package lk.ijse.etecmanagementsystem.dto;

public class CategoryDTO {
    private String name;
    private String description;

    public CategoryDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public  void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
