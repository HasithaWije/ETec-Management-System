package lk.ijse.etecmanagementsystem.dto.tm;

import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.util.RepairStatus;
import java.text.SimpleDateFormat;

public class RepairJobTM {

    // Simple fields (No more 'IntegerProperty', etc.)
    private int repairId;
    private String customerName;
    private String contactNumber;
    private String deviceName;
    private String serialNumber;
    private String problemDescription;
    private RepairStatus status;
    private String dateInFormatted;

    // Keep reference to original DTO for Database operations
    private RepairJobDTO originalDto;

    public RepairJobTM(RepairJobDTO dto, String cusName, String cusContact) {
        this.originalDto = dto;

        this.repairId = dto.getRepairId();
        this.customerName = cusName;
        this.contactNumber = cusContact;
        this.deviceName = dto.getDeviceName();
        this.serialNumber = dto.getDeviceSn();
        this.problemDescription = dto.getProblemDesc();
        this.status = dto.getStatus();

        // Format Date string once
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.dateInFormatted = (dto.getDateIn() != null) ? sdf.format(dto.getDateIn()) : "N/A";
    }

    // --- Standard Getters & Setters ---

    public int getRepairId() {
        return repairId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
        // Sync with original DTO immediately
        if (this.originalDto != null) {
            this.originalDto.setStatus(status);
        }
    }

    public String getDateInFormatted() {
        return dateInFormatted;
    }

    public RepairJobDTO getOriginalDto() {
        // Sync text fields back to DTO before saving
        originalDto.setProblemDesc(this.problemDescription);
        return originalDto;
    }
}