package lk.ijse.etecmanagementsystem.dto.tm;


import lk.ijse.etecmanagementsystem.util.ProductCondition;

public class RepairPartTM {

    private final int itemId;
    private final String itemName;
    private final String serialNumber;
    private final ProductCondition condition; // <--- NEW FIELD
    private final double unitPrice;

    public RepairPartTM(int itemId, String itemName, String serialNumber, ProductCondition condition, double unitPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.serialNumber = serialNumber;
        this.condition = condition; // <--- NEW ASSIGNMENT
        this.unitPrice = unitPrice;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getSerialNumber() { return serialNumber; }

    public ProductCondition getCondition() { return condition; } // <--- NEW GETTER

    public double getUnitPrice() { return unitPrice; }
}