package lk.ijse.etecmanagementsystem.dto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProductItemDTO {
    private  int itemId;
    private int stockId;
    private int supplierId;
    private String serialNumber;
    private String productName;   // Added for Tab 3/4 context
    private String supplierName;
    private int supplierWarranty;
    private int customerWarranty;
    private String status;

    // Raw Dates for Calculation
    private Date addedDate;
    private Date soldDate;

    public ProductItemDTO() {
    }

    public ProductItemDTO(int stockId, String serialNumber, int customerWarranty) {
        this.stockId = stockId;
        this.serialNumber = serialNumber;
        this.customerWarranty = customerWarranty;
    }

    // Full Constructor
    public ProductItemDTO(String serialNumber, String productName, String supplierName,
                          int supplierWarranty, int customerWarranty, String status,
                          Date addedDate, Date soldDate) {
        this.serialNumber = serialNumber;
        this.productName = productName;
        this.supplierName = supplierName;
        this.supplierWarranty = supplierWarranty;
        this.customerWarranty = customerWarranty;
        this.status = status;
        this.addedDate = addedDate;
        this.soldDate = soldDate;
    }

    public ProductItemDTO(String serialNumber, String productName, int customerWarranty, String status) {
        this.serialNumber = serialNumber;
        this.productName = productName;
        this.customerWarranty = customerWarranty;
        this.status = status;
    }

    public ProductItemDTO(int itemId, int stockId, int supplierId, String serialNumber, String productName, String supplierName, int supplierWarranty, int customerWarranty, String status, Date addedDate, Date soldDate) {
        this.itemId = itemId;
        this.stockId = stockId;
        this.supplierId = supplierId;
        this.serialNumber = serialNumber;
        this.productName = productName;
        this.supplierName = supplierName;
        this.supplierWarranty = supplierWarranty;
        this.customerWarranty = customerWarranty;
        this.status = status;
        this.addedDate = addedDate;
        this.soldDate = soldDate;
    }

    public ProductItemDTO(int stockId, int supplierWarranty) {
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public int getSupplierWarranty() {
        return supplierWarranty;
    }

    public void setSupplierWarranty(int supplierWarranty) {
        this.supplierWarranty = supplierWarranty;
    }

    public int getCustomerWarranty() {
        return customerWarranty;
    }

    public void setCustomerWarranty(int customerWarranty) {
        this.customerWarranty = customerWarranty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Date getSoldDate() {
        return soldDate;
    }

    public void setSoldDate(Date soldDate) {
        this.soldDate = soldDate;
    }

    @Override
    public String toString() {
        return "ProductItemDTO{" +
                "itemId=" + itemId +
                ", stockId=" + stockId +
                ", supplierId=" + supplierId +
                ", serialNumber='" + serialNumber + '\'' +
                ", productName='" + productName + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", supplierWarranty=" + supplierWarranty +
                ", customerWarranty=" + customerWarranty +
                ", status='" + status + '\'' +
                ", addedDate=" + addedDate +
                ", soldDate=" + soldDate +
                '}';
    }

    // Logic: Calculate Remaining Life
    public String getRemainingLife() {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        int warrantyMonths;
        String type;

        if ("SOLD".equalsIgnoreCase(status) && soldDate != null) {
            startDate = soldDate.toLocalDate();
            warrantyMonths = customerWarranty;
            type = "(Cust)";
        } else {
            if (addedDate == null) return "Unknown";
            startDate = addedDate.toLocalDate();
            warrantyMonths = supplierWarranty;
            type = "(Sup)";
        }

        LocalDate endDate = startDate.plusMonths(warrantyMonths);
        long monthsLeft = ChronoUnit.MONTHS.between(today, endDate);
        long daysLeft = ChronoUnit.DAYS.between(today, endDate);

        if (daysLeft < 0) return "EXPIRED " + type;
        if (monthsLeft < 1) return daysLeft + " Days " + type;
        return monthsLeft + " Months " + type;
    }

}