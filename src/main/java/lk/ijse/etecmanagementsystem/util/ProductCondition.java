package lk.ijse.etecmanagementsystem.util;


public enum ProductCondition {

    USED("Used"),
    BRAND_NEW("Brand New"),
    BOTH("New & Used");

    private final String label;


    ProductCondition(String label) {
        this.label = label;
    }


    public String getLabel() {
        return label;
    }

    // This is CRITICAL for JavaFX ComboBoxes.
    // It tells the UI to display "Brand New" instead of "BRAND_NEW"
    @Override
    public String toString() {
        return label;
    }
}