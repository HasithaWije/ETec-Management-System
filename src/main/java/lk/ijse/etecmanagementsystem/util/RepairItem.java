package lk.ijse.etecmanagementsystem.util;


public class RepairItem {
    private String name;
    private int qty;
    private double unitPrice;

    public RepairItem(String name, int qty, double unitPrice) {
        this.name = name;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    public String getName() { return name; }
    public int getQty() { return qty; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotal() { return qty * unitPrice; }
}