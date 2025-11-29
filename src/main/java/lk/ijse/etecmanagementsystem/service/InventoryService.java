package lk.ijse.etecmanagementsystem.service;

import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static lk.ijse.etecmanagementsystem.util.ProductCondition.BOTH;
import static lk.ijse.etecmanagementsystem.util.ProductCondition.USED;

public class InventoryService {

    private final List<ProductDTO> masterList = new ArrayList<>();

    public InventoryService() {
        loadDummyData();
    }

    private void loadDummyData() {
        // Simulating Database Fetch
        for (int i = 1; i <= 500; i++) {
            String cat = (i % 3 == 0) ? "Electronics" : (i % 2 == 0) ? "Accessories" : "Parts";
            masterList.add(new ProductDTO("p" + i, "Item 2222222222222222222222222" + i, "description about the product, must be under the three line", 1000 + (i * 50), cat, "placeholder.png", 800 + (i * 40), 12 + (i % 24), ProductCondition.USED, (i % 50)));
        }
        masterList.add(new ProductDTO("p5555","MacBook Pro","gg",450000,"Electronics","macbook.png", 10,12, ProductCondition.BRAND_NEW,10));
    }


    public List<ProductDTO> getFilteredProducts(String searchText, String category, ProductCondition value) {
        // 1. Handle potential null search text and normalize to lowercase once
        String finalSearch = (searchText == null) ? "" : searchText.toLowerCase();

        return masterList.stream()
                // Filter 1: Search Text (using the pre-calculated finalSearch)
                .filter(p -> p.getName().toLowerCase().contains(finalSearch))

                // Filter 2: Category
                .filter(p -> isCategoryMatch(p, category))

                // Filter 3: Condition
                .filter(p -> isConditionMatch(p, value))

                // Sorting and Collecting
                .sorted(Comparator.comparing(ProductDTO::getName))
                .collect(Collectors.toList());
    }

    // Helper method for readability
    private boolean isCategoryMatch(ProductDTO p, String category) {
        // Keep if category is null, "All", or matches the product
        return category == null
                || category.equals("All Categories")
                || p.getCategory().equals(category);
    }

    // Helper method for readability
    private boolean isConditionMatch(ProductDTO p, ProductCondition value) {
        // Keep if value is null, "BOTH", or matches the product
        return value == null
                || value == ProductCondition.BOTH
                || p.getCondition() == value;
    }
}