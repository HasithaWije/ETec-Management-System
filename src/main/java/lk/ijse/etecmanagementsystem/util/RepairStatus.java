package lk.ijse.etecmanagementsystem.util;

public enum RepairStatus {
    PENDING("PENDING"), DIAGNOSIS("DIAGNOSIS"), WAITING_PARTS("WAITING_PARTS"),
    COMPLETED("COMPLETED"), DELIVERED("DELIVERED"), CANCELLED("CANCELLED");

    private final String label;

    RepairStatus() {
        this.label = this.name();
    }

    RepairStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
