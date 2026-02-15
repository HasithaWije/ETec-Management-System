package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lk.ijse.etecmanagementsystem.dao.CategoryDAOImpl;
import lk.ijse.etecmanagementsystem.util.Category;

import java.util.List;

public class CategoryController {

    @FXML
    TextField categoryName;
    @FXML
    Button btnSave;
    @FXML
    Button btnDelete;
    @FXML
    Button btnUpdate;
    @FXML
    Button btnReset;
    @FXML
    Label lblMsg;

    CategoryDAOImpl categoryDAO = new CategoryDAOImpl();
    private String previousCategoryName = "";

    private final String CATEGORY_REGEX = "^[A-Z][a-zA-Z0-9\\s\\-&]{2,29}$";

    @FXML
    public void initialize() {
        System.out.println("CategoryController initialized");
        loadCategories();

    }

    private void loadCategories() {


        try {
            List<String> list = categoryDAO.getAllCategories();
            if (!list.isEmpty()) {
                Category.getCategories().clear();// Clear existing categories
                Category.getCategories().setAll(list);

                System.out.println("Categories loaded from DB: " + list);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No categories found in the database.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load categories: " + e.getMessage());
            alert.showAndWait();
        }


    }

    @FXML
    private void addCategory() {
        System.out.println("Add category method called");
        String name = categoryName.getText().trim();
        if (name.isEmpty()) {
            lblMsg.setText("Category name cannot be empty.");
            lblMsg.setStyle("-fx-text-fill: red;");
        } else {
            ObservableList<String> categories = Category.getCategories();
            if (!name.matches(CATEGORY_REGEX)) {
                lblMsg.setText("Invalid category name. It should start with an uppercase letter and be 3-30 characters long.");
                lblMsg.setStyle("-fx-text-fill: red;");


            } else if (categories.contains(name) && !checkNomatch(name)) {
                lblMsg.setText("Category Already exists.");
                lblMsg.setStyle("-fx-text-fill: red;");

            } else {

                try {

                    if (categoryDAO.saveCategory(name)) {
                        lblMsg.setText("Category " + name + " added successfully.");
                        lblMsg.setStyle("-fx-text-fill: green;");

                        categoryName.clear();
                        loadCategories();
                        return;
                    }

                    lblMsg.setText("Category add failed.");
                    lblMsg.setStyle("-fx-text-fill: red;");

                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save category: " + e.getMessage()).show();
                }
            }
        }
    }

    @FXML
    private void deleteCategory() {
        System.out.println("Delete category method called");
        String name = previousCategoryName;
        if (name.isEmpty()) {
            lblMsg.setText("You must search and select a category to delete (Press Enter).");
            lblMsg.setStyle("-fx-text-fill: red;");
        } else {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to delete category " + name + "?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                try {


                    boolean result = categoryDAO.deleteCategory(name);
                    if (!result) {
                        lblMsg.setText("Category delete failed.");
                        lblMsg.setStyle("-fx-text-fill: red;");
                        return;
                    }

                    lblMsg.setText("Category " + name + " delete successfully.");
                    lblMsg.setStyle("-fx-text-fill: green;");

                    categoryName.clear();
                    loadCategories();

                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save category: " + e.getMessage()).show();
                }
            } else {
                lblMsg.setText("Category deletion cancelled.");
                lblMsg.setStyle("-fx-text-fill: orange;");
            }

        }
    }

    @FXML
    private void updateCategory() {
        System.out.println("Update category method called");
        String oldName = previousCategoryName;
        String newName = categoryName.getText().trim();
        if (oldName.isEmpty()) {
            lblMsg.setText("You must search and select a category to Update (Press Enter).");
            lblMsg.setStyle("-fx-text-fill: red;");
        } else {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to update category " + oldName + " to " + newName + "?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                try {

                    boolean result = categoryDAO.updateCategory(newName, oldName);
                    if (!result) {
                        lblMsg.setText("Category update failed.");
                        lblMsg.setStyle("-fx-text-fill: red;");
                        return;
                    }
                    lblMsg.setText("Category " + newName + " update successfully.");
                    lblMsg.setStyle("-fx-text-fill: green;");

                    categoryName.clear();
                    loadCategories();

                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save category: " + e.getMessage()).show();
                }
            } else {
                lblMsg.setText("Category update cancelled.");
                lblMsg.setStyle("-fx-text-fill: orange;");
            }

        }
    }

    private boolean checkNomatch(String name) {

        return Category.getCategories().stream()
                .noneMatch(category -> category.equalsIgnoreCase(name));// Check for case-insensitive match
        // If no match found, return true (no similar category exists)

    }

    @FXML
    private void resetCategory() {
        System.out.println("Reset category method called");
        categoryName.clear();
        lblMsg.setText("Message: Type Category and Press Enter to Find.");
    }

    @FXML
    private void getEnterkey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String name = categoryName.getText().trim();

            if (categoryName.getText().isEmpty()) {
                lblMsg.setText("Category name cannot be empty.");
                lblMsg.setStyle("-fx-text-fill: red;");

            } else {
                ObservableList<String> categories = Category.getCategories();

                if (!name.matches(CATEGORY_REGEX)) {
                    lblMsg.setText("Invalid category name. It should start with an uppercase letter and be 3-30 characters long.");
                    lblMsg.setStyle("-fx-text-fill: red;");

                } else if (categories.contains(name) || !checkNomatch(name)) {

                    int index = categories.indexOf(name);

                    if (index != -1) {
                        categoryName.setText(categories.get(index));
                        previousCategoryName = categories.get(index);
                        lblMsg.setText("Category " + name + " Found! You can delete or update now.");
                        lblMsg.setStyle("-fx-text-fill: green;");
                    }

                } else {
                    lblMsg.setText("Category not exists.");
                    lblMsg.setStyle("-fx-text-fill: red;");

                }

            }

        }
    }
}
