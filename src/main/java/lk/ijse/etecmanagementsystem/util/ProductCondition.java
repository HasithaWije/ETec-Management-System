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

        return switch (text) {
            case "BRAND NEW" -> BRAND_NEW;
            case "USED" -> USED;
            default -> BOTH;
        };
    }


    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}