package lk.ijse.etecmanagementsystem.util;


public enum ProductCondition {

    USED("USED"),
    BRAND_NEW("BRAND NEW"),
    BOTH("NOT SET");

    private final String label;


    ProductCondition(String label) {
        this.label = label;
    }

    public static ProductCondition fromString(String text) {
        if (text == null) return BOTH;

        switch (text) {
            case "BRAND NEW": return BRAND_NEW;
            case "USED":      return USED;
            default:          return BOTH;
        }
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