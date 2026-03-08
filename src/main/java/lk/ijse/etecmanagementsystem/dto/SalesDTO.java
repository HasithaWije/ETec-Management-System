package lk.ijse.etecmanagementsystem.dto;

import lk.ijse.etecmanagementsystem.util.PaymentStatus;

import java.sql.Timestamp;
import java.util.Date;

public class SalesDTO {
    private int saleId;
    private  int customerId;
    private int userId;
    private int qty;
    private Date saleDate;
    private double subtotal;
    private double discount;
    private double grandTotal;
    private double paidAmount;
    private int customerWarrantyMonths;
    private PaymentStatus paymentStatus;
    private String description;

    public SalesDTO() {
    }

    public SalesDTO(int saleId, int qty, Date saleDate, double subtotal, double discount, double grandTotal, int customerWarrantyMonths, PaymentStatus paymentStatus, String description) {
        this.saleId = saleId;
        this.qty = qty;
        this.saleDate = saleDate;
        this.subtotal = subtotal;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.customerWarrantyMonths = customerWarrantyMonths;
        this.paymentStatus = paymentStatus;
        this.description = description;
    }

    public SalesDTO(int saleId, int customerId, int userId, int qty, Date saleDate, double subtotal, double discount, double grandTotal, double paidAmount, int customerWarrantyMonths, PaymentStatus paymentStatus, String description) {
        this.saleId = saleId;
        this.customerId = customerId;
        this.userId = userId;
        this.qty = qty;
        this.saleDate = saleDate;
        this.subtotal = subtotal;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.paidAmount = paidAmount;
        this.customerWarrantyMonths = customerWarrantyMonths;
        this.paymentStatus = paymentStatus;
        this.description = description;
    }

    public SalesDTO(int saleId, int customerId, int userId, Date saleDate, double subTotal, double discount, double grandTotal, double paidAmount, String paymentStatus, String description) {
        this.saleId = saleId;
        this.customerId = customerId;
        this.userId = userId;
        this.saleDate = saleDate;
        this.subtotal = subTotal;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.paidAmount = paidAmount;
        this.paymentStatus = PaymentStatus.valueOf(paymentStatus);
        this.description = description;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Date saleDate) {
        this.saleDate = saleDate;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public int getCustomerWarrantyMonths() {

        return customerWarrantyMonths;
    }

    public void setCustomerWarrantyMonths(int customerWarrantyMonths) {
        this.customerWarrantyMonths = customerWarrantyMonths;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    @Override
    public String toString() {
        return "SalesDTO{" +
                "saleId=" + saleId +
                ", customerId=" + customerId +
                ", userId=" + userId +
                ", qty=" + qty +
                ", saleDate=" + saleDate +
                ", subtotal=" + subtotal +
                ", discount=" + discount +
                ", grandTotal=" + grandTotal +
                ", paidAmount=" + paidAmount +
                ", customerWarrantyMonths=" + customerWarrantyMonths +
                ", paymentStatus=" + paymentStatus +
                ", description='" + description + '\'' +
                '}';
    }
}
