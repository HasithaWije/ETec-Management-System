//package lk.ijse.etecmanagementsystem.service;
//
//import javafx.collections.ObservableList;
//import lk.ijse.etecmanagementsystem.dto.ProductDTO;
//import lk.ijse.etecmanagementsystem.util.ProductCondition;
//import lk.ijse.etecmanagementsystem.util.ProductUtil;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static lk.ijse.etecmanagementsystem.util.ProductCondition.*;
//
//public class InventoryUtil {
//
//    private final List<ProductDTO> masterList = new ArrayList<>();
//
//    public InventoryUtil() {
//        masterList.addAll(ProductUtil.productCache);
//    }
//
//    private void loadDummyData() {
//        for (int i = 1; i <= 500; i++) {
//            String cat = (i % 3 == 0) ? "Electronics" : (i % 2 == 0) ? "Accessories" : "Parts";
//            masterList.add(new ProductDTO(
//                    "P" + String.format("%03d", i),
//                    "Product " + i,
//                    "Description for Product " + i,
//                    50.0 + i,
//                    cat,
//                    (i % 2 == 0) ? ProductCondition.BRAND_NEW : USED,
//                    12,
//                    100 + i
//            ));
//        }
//    }
//
//    public List<ProductDTO> getFilteredProducts(String searchText, String category, ProductCondition value) {
//
//        String finalSearch = (searchText == null) ? "" : searchText.toLowerCase();
//
//        return masterList.stream()
//
//                .filter(p -> p.getName().toLowerCase().contains(finalSearch))
//
//
//                .filter(p -> isCategoryMatch(p, category))
//
//
//                .filter(p -> isConditionMatch(p, value))
//
//
//                .sorted(Comparator.comparing(ProductDTO::getName))
//                .collect(Collectors.toList());
//    }
//
//
//    private boolean isCategoryMatch(ProductDTO p, String category) {
//
//        return category == null
//                || category.equals("All Categories")
//                || p.getCategory().equals(category);
//    }
//
//
//    private boolean isConditionMatch(ProductDTO p, ProductCondition value) {
//
//        if(p.getCondition().equals(value)) {
//            return true;
//        } else if(value == BOTH || value == null) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//}
package lk.ijse.etecmanagementsystem.util;

import javafx.collections.ObservableList;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryUtil {

    public List<ProductDTO> getFilteredProducts(ObservableList<ProductDTO> productDataList, String searchText, String category, ProductCondition conditionFilter, Stock stockFilter) {

        String finalSearch = (searchText == null) ? "" : searchText.toLowerCase();

        return productDataList.stream()
                // 1. Filter by Name
                .filter(p -> p.getName().toLowerCase().contains(finalSearch))

                // 2. Filter by Category
                .filter(p -> isCategoryMatch(p, category))

                // 3. Filter by Condition (Enum)
                .filter(p -> isConditionMatch(p, conditionFilter))

                // 4. Filter by Stock Status
                .filter(p -> isStockMatch(p, stockFilter))

                .collect(Collectors.toList());
    }

    private boolean isCategoryMatch(ProductDTO p, String category) {
        return category == null
                || category.equals("All Categories")
                || p.getCategory().equals(category);
    }

    private boolean isConditionMatch(ProductDTO p, ProductCondition filterValue) {
        if (filterValue == null || filterValue == ProductCondition.BOTH) {
            return true;
        } else if (filterValue == ProductCondition.BRAND_NEW) {
            return p.getCondition().equals(ProductCondition.BRAND_NEW);
        } else if (filterValue == ProductCondition.USED) {
            return p.getCondition().equals(ProductCondition.USED);
        }
        return true;
    }

    private boolean isStockMatch(ProductDTO p, Stock stock) {
        if (stock == null || stock == Stock.ALL) {
            return true;
        } else if (stock == Stock.IN) {
            return p.getQty() > 0;
        } else if (stock == Stock.OUT) {
            return p.getQty() == 0;
        }
        return true;
    }
}