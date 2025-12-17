package lk.ijse.etecmanagementsystem.dto.tm;

public class ItemCartTM {
    private int itemId;
    private String itemName;
    private String serialNo;
    private int warrantyMonths;
    private  int quantity;
    private String condition;
    private double unitPrice;
    private double discount;
    private double total; // (Price - Discount)

    public ItemCartTM() {
    }

    public ItemCartTM(int itemId, String itemName, String serialNo, int warrantyMonths, int quantity, String condition, double unitPrice, double discount, double total) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.serialNo = serialNo;
        this.warrantyMonths = warrantyMonths;
        this.quantity = quantity;
        this.condition = condition;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.total = total;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ItemCartTM{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", serialNo='" + serialNo + '\'' +
                ", warrantyMonths=" + warrantyMonths +
                ", quantity=" + quantity +
                ", condition='" + condition + '\'' +
                ", unitPrice=" + unitPrice +
                ", discount=" + discount +
                ", total=" + total +
                '}';
    }
}
