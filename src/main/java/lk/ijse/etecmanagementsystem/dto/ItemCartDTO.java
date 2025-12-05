package lk.ijse.etecmanagementsystem.dto;

public class ItemCartDTO {
    private int itemId;
    private String itemName;
    private String serialNo;
    private double unitPrice;
    private double discount;
    private double total; // (Price - Discount)

    public ItemCartDTO() {
    }

    public ItemCartDTO(int itemId, String itemName, String serialNo, double unitPrice, double discount, double total) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.serialNo = serialNo;
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
        return "ItemCartDTO{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", serialNo='" + serialNo + '\'' +
                ", unitPrice=" + unitPrice +
                ", discount=" + discount +
                ", total=" + total +
                '}';
    }
}
