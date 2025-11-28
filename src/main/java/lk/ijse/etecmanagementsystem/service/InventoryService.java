package lk.ijse.etecmanagementsystem.service;

import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {

    private final List<ProductDTO> masterList = new ArrayList<>();

    public InventoryService() {
        loadDummyData();
    }

    private void loadDummyData() {
        // Simulating Database Fetch
        for (int i = 1; i <= 500; i++) {
            String cat = (i % 3 == 0) ? "Electronics" : (i % 2 == 0) ? "Accessories" : "Parts";
            masterList.add(new ProductDTO("p"+i,"Item " + i, 1000 + (i * 50), cat, "placeholder.png", 800 + (i * 40), 12 + (i % 24), 10 + (i % 50)));
        }
        masterList.add(new ProductDTO("MacBook Pro", 450000, "Electronics", "placeholder.png"));
    }

    public List<ProductDTO> getFilteredProducts(String searchText, String category) {
        String finalSearch = searchText.toLowerCase();


        return masterList.stream().filter(p -> p.getName().toLowerCase().contains(searchText)) // Search Name
                .filter(p -> {
                    if (category == null || category.equals("All Categories")) return true;
                    return p.getCategory().equals(category); // Filter Category
                }).sorted(Comparator.comparing(ProductDTO::getName)) // Sort A-Z (Ascending)
                .collect(Collectors.toList());
    }
}