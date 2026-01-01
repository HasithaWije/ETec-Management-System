package lk.ijse.etecmanagementsystem.dto;

import lk.ijse.etecmanagementsystem.util.PaymentStatus;
import lk.ijse.etecmanagementsystem.util.RepairStatus;
import java.util.Date;

public class RepairJobDTO {
    private int repairId;
    private int cusId;
    private int userId;
    private String deviceName;
    private String deviceSn;
    private String problemDesc;
    private String diagnosisDesc;
    private String repairResults;
    private RepairStatus status;
    private Date dateIn;
    private Date dateOut;
    private double laborCost;
    private double partsCost;
    private double totalAmount;
    private double paidAmount;
    private double discount;
    private PaymentStatus paymentStatus;

    public RepairJobDTO() {}

    public RepairJobDTO(int repairId, int cusId, int userId, String deviceName, String deviceSn, String problemDesc,String diagnosisDesc,String repairResults, RepairStatus status, Date dateIn, Date dateOut, double laborCost, double partsCost, double totalAmount, double discount, double paidAmount, PaymentStatus paymentStatus) {
        this.repairId = repairId;
        this.cusId = cusId;
        this.userId = userId;
        this.deviceName = deviceName;
        this.deviceSn = deviceSn;
        this.problemDesc = problemDesc;
        this.diagnosisDesc = diagnosisDesc;
        this.repairResults = repairResults;
        this.status = status;
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.laborCost = laborCost;
        this.partsCost = partsCost;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.paymentStatus = paymentStatus;
    }

    // Getters and Setters
    public int getRepairId() { return repairId; }
    public void setRepairId(int repairId) { this.repairId = repairId; }

    public int getCusId() { return cusId; }
    public void setCusId(int cusId) { this.cusId = cusId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceSn() { return deviceSn; }
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }

    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String problemDesc) { this.problemDesc = problemDesc; }

    public RepairStatus getStatus() { return status; }
    public void setStatus(RepairStatus status) { this.status = status; }

    public Date getDateIn() { return dateIn; }
    public void setDateIn(Date dateIn) { this.dateIn = dateIn; }

    public Date getDateOut() { return dateOut; }
    public void setDateOut(Date dateOut) { this.dateOut = dateOut; }

    public double getLaborCost() { return laborCost; }
    public void setLaborCost(double laborCost) { this.laborCost = laborCost; }

    public double getPartsCost() { return partsCost; }
    public void setPartsCost(double partsCost) { this.partsCost = partsCost; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public  double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount;}

    public String getDiagnosisDesc() { return diagnosisDesc; }
    public void setDiagnosisDesc(String diagnosisDesc) { this.diagnosisDesc = diagnosisDesc; }

    public String getRepairResults() { return repairResults; }
    public void setRepairResults(String repairResults) { this.repairResults = repairResults; }

    @Override
    public String toString() {
        return "RepairJobDTO{" +
                "repairId=" + repairId +
                ", cusId=" + cusId +
                ", userId=" + userId +
                ", deviceName='" + deviceName + '\'' +
                ", deviceSn='" + deviceSn + '\'' +
                ", problemDesc='" + problemDesc + '\'' +
                ", diagnosisDesc='" + diagnosisDesc + '\'' +
                ", repairResults='" + repairResults + '\'' +
                ", status=" + status +
                ", dateIn=" + dateIn +
                ", dateOut=" + dateOut +
                ", laborCost=" + laborCost +
                ", partsCost=" + partsCost +
                ", totalAmount=" + totalAmount +
                ", paidAmount=" + paidAmount +
                ", discount=" + discount +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}