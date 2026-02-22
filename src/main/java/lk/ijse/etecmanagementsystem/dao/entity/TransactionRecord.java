package lk.ijse.etecmanagementsystem.dao.entity;

public class TransactionRecord {
    int transaction_id;
    int sale_id;
    int repair_id;
    int customer_id;
    int user_id;
    int transaction_date;
    String transaction_type;
    String transaction_method;
    double amount;
    String flow;
    String reference_note;

    public TransactionRecord() {
    }

    public TransactionRecord(int transaction_id, int sale_id, int repair_id, int customer_id, int user_id, int transaction_date, String transaction_type, String transaction_method, double amount, String flow, String reference_note) {
        this.transaction_id = transaction_id;
        this.sale_id = sale_id;
        this.repair_id = repair_id;
        this.customer_id = customer_id;
        this.user_id = user_id;
        this.transaction_date = transaction_date;
        this.transaction_type = transaction_type;
        this.transaction_method = transaction_method;
        this.amount = amount;
        this.flow = flow;
        this.reference_note = reference_note;
    }


    public TransactionRecord(int sale_id, int customer_id, int user_id, String transaction_type, String transaction_method, double amount, String flow, String reference_note) {
        this.sale_id = sale_id;
        this.customer_id = customer_id;
        this.user_id = user_id;
        this.transaction_type = transaction_type;
        this.transaction_method = transaction_method;
        this.amount = amount;
        this.flow = flow;
        this.reference_note = reference_note;
    }

    public int getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public int getSale_id() {
        return sale_id;
    }

    public void setSale_id(int sale_id) {
        this.sale_id = sale_id;
    }

    public int getRepair_id() {
        return repair_id;
    }

    public void setRepair_id(int repair_id) {
        this.repair_id = repair_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(int transaction_date) {
        this.transaction_date = transaction_date;
    }

    public String getTransaction_type() {
        return transaction_type;
    }

    public void setTransaction_type(String transaction_type) {
        this.transaction_type = transaction_type;
    }

    public String getTransaction_method() {
        return transaction_method;
    }

    public void setTransaction_method(String transaction_method) {
        this.transaction_method = transaction_method;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getReference_note() {
        return reference_note;
    }

    public void setReference_note(String reference_note) {
        this.reference_note = reference_note;
    }
}
