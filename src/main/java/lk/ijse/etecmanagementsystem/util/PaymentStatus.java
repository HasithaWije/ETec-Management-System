package lk.ijse.etecmanagementsystem.util;

public enum PaymentStatus {
    PENDING("PENDING"), PARTIAL("PARTIAL"), PAID("PAID");

    private final String label;

    PaymentStatus() {
        this.label = this.name();
    }
    PaymentStatus(String label) {
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
