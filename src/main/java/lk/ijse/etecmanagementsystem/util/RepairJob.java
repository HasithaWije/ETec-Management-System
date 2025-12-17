package lk.ijse.etecmanagementsystem.util;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;

public class RepairJob {
    private int id;
    private String customerName;
    private String customerPhone;
    private String deviceName;
    private String serialNumber;
    private String problemDescription;
    private String diagnosisNote = "";
    private String completionNote = "";

    private RepairStatus status;
    private String paymentStatus = "PENDING";

    private double laborCost = 0.0;
    private ObservableList<RepairItem> items = FXCollections.observableArrayList();
    private LocalDateTime dateIn;

    public RepairJob(int id, String cusName, String cusPhone, String devName, String sn, String problem) {
        this.id = id;
        this.customerName = cusName;
        this.customerPhone = cusPhone;
        this.deviceName = devName;
        this.serialNumber = sn;
        this.problemDescription = problem;
        this.status = RepairStatus.PENDING;
        this.dateIn = LocalDateTime.now();
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getDeviceName() { return deviceName; }
    public String getSerialNumber() { return serialNumber; }
    public String getProblemDescription() { return problemDescription; }
    public String getDiagnosisNote() { return diagnosisNote; }
    public void setDiagnosisNote(String diagnosisNote) { this.diagnosisNote = diagnosisNote; }
    public String getCompletionNote() { return completionNote; }
    public void setCompletionNote(String completionNote) { this.completionNote = completionNote; }
    public RepairStatus getStatus() { return status; }
    public void setStatus(RepairStatus status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public double getLaborCost() { return laborCost; }
    public void setLaborCost(double laborCost) { this.laborCost = laborCost; }
    public ObservableList<RepairItem> getItems() { return items; }

    public double getPartsTotal() {
        return items.stream().mapToDouble(RepairItem::getTotal).sum();
    }

    public double getGrandTotal() {
        return getPartsTotal() + laborCost;
    }
}