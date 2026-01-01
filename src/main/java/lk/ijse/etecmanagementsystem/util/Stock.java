package lk.ijse.etecmanagementsystem.util;

public enum Stock {
    ALL("STOCK-ALL"),
    IN("STOCK-IN"),
    OUT("STOCK-OUT");

    private final String stockType;

    Stock(String stockType) {
        this.stockType = stockType;
    }

    public String getStockType() {
        return stockType;
    }

    @Override
    public String toString() {
        return stockType;
    }
}
