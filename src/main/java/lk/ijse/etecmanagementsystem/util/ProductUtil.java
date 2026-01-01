package lk.ijse.etecmanagementsystem.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;


public class ProductUtil {


    public static final ObservableList<ProductDTO> productCache;

    static {
        productCache = FXCollections.observableArrayList();
    }


}
