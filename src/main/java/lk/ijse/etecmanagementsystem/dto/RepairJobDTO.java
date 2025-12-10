package lk.ijse.etecmanagementsystem.dto;

import lk.ijse.etecmanagementsystem.util.PaymentStatus;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.util.Date;

public class RepairJobDTO {
    private int repairId;
    private int cusId;
    private int userId;
    private String deviceName;
    private String device_sn;
    private String problem_desc;
    private RepairStatus status;       //  ENUM ('PENDING', 'DIAGNOSIS', 'WAITING_PARTS', 'COMPLETED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',

    private Date dateIn;
    private Date dateOut;

    private double laborCost;
    private double partsCost;
    private double totalCost;

    private PaymentStatus paymentStatus; // ('PENDING', 'PARTIAL', 'PAID')

    public RepairJobDTO() {}

    public RepairJobDTO(int repairId, int cusId, int userId, String deviceName, String device_sn, String problem_desc, RepairStatus status, Date dateIn, Date dateOut, double laborCost, double partsCost, double totalCost, PaymentStatus paymentStatus) {
        this.repairId = repairId;
        this.cusId = cusId;
        this.userId = userId;
        this.deviceName = deviceName;
        this.device_sn = device_sn;
        this.problem_desc = problem_desc;
        this.status = status;
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.laborCost = laborCost;
        this.partsCost = partsCost;
        this.totalCost = totalCost;
        this.paymentStatus = paymentStatus;
    }

    public RepairJobDTO(int repairId, String deviceName, String device_sn, String problem_desc, RepairStatus status, Date dateIn, Date dateOut, double laborCost, double partsCost, double totalCost, PaymentStatus paymentStatus) {
        this.repairId = repairId;
        this.deviceName = deviceName;
        this.device_sn = device_sn;
        this.problem_desc = problem_desc;
        this.status = status;
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.laborCost = laborCost;
        this.partsCost = partsCost;
        this.totalCost = totalCost;
        this.paymentStatus = paymentStatus;
    }

    public int getRepairId() {
        return repairId;
    }

    public void setRepairId(int repairId) {
        this.repairId = repairId;
    }

    public int getCusId() {
        return cusId;
    }

    public void setCusId(int cusId) {
        this.cusId = cusId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevice_sn() {
        return device_sn;
    }

    public void setDevice_sn(String device_sn) {
        this.device_sn = device_sn;
    }

    public String getProblem_desc() {
        return problem_desc;
    }

    public void setProblem_desc(String problem_desc) {
        this.problem_desc = problem_desc;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public Date getDateIn() {
        return dateIn;
    }

    public void setDateIn(Date dateIn) {
        this.dateIn = dateIn;
    }

    public Date getDateOut() {
        return dateOut;
    }

    public void setDateOut(Date dateOut) {
        this.dateOut = dateOut;
    }

    public double getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(double laborCost) {
        this.laborCost = laborCost;
    }

    public double getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(double partsCost) {
        this.partsCost = partsCost;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "RepairJobDTO{" +
                "repairId=" + repairId +
                ", cusId=" + cusId +
                ", userId=" + userId +
                ", deviceName='" + deviceName + '\'' +
                ", device_sn='" + device_sn + '\'' +
                ", problem_desc='" + problem_desc + '\'' +
                ", status=" + status +
                ", dateIn=" + dateIn +
                ", dateOut=" + dateOut +
                ", laborCost=" + laborCost +
                ", partsCost=" + partsCost +
                ", totalCost=" + totalCost +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
