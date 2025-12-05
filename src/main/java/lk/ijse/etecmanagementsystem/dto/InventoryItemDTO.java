package lk.ijse.etecmanagementsystem.dto;

public class InventoryItemDTO {

    private  int itemId;
    private  String itemName;
    private  String productName;
    private  String serialNumber;
    private  int customerWarranty;
    private  double itemPrice;
    private  String status;


    public InventoryItemDTO() {
    }

    public InventoryItemDTO(int itemId, String productName, String serialNumber, int customerWarranty, double itemPrice, String status) {
        this.itemId = itemId;
        this.productName = productName;
        this.serialNumber = serialNumber;
        this.customerWarranty = customerWarranty;
        this.itemPrice = itemPrice;
        this.status = status;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getCustomerWarranty() {
        return customerWarranty;
    }

    public void setCustomerWarranty(int customerWarranty) {
        this.customerWarranty = customerWarranty;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "InventoryItemDTO{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", productName='" + productName + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", customerWarranty=" + customerWarranty +
                ", itemPrice=" + itemPrice +
                ", status='" + status + '\'' +
                '}';
    }
}
