package lk.ijse.etecmanagementsystem.entity;

import java.util.Date;

public class Sales {
    private int sale_id;
    private int customer_id;
    private int user_id;
    private Date sale_date;
    private double sub_total;
    private double discount;
    private double grand_total;
    private double paid_amount;
    private String payment_status;
    private String description;

    public Sales() {
    }

    public Sales(int sale_id, int customer_id, int user_id, Date sale_date, double sub_total, double discount, double grand_total, double paid_amount, String payment_status, String description) {
        this.sale_id = sale_id;
        this.customer_id = customer_id;
        this.user_id = user_id;
        this.sale_date = sale_date;
        this.sub_total = sub_total;
        this.discount = discount;
        this.grand_total = grand_total;
        this.paid_amount = paid_amount;
        this.payment_status = payment_status;
        this.description = description;
    }

    public int getSale_id() {
        return sale_id;
    }

    public void setSale_id(int sale_id) {
        this.sale_id = sale_id;
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

    public Date getSale_date() {
        return sale_date;
    }

    public void setSale_date(Date sale_date) {
        this.sale_date = sale_date;
    }

    public double getSub_total() {
        return sub_total;
    }

    public void setSub_total(double sub_total) {
        this.sub_total = sub_total;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getGrand_total() {
        return grand_total;
    }

    public void setGrand_total(double grand_total) {
        this.grand_total = grand_total;
    }

    public double getPaid_amount() {
        return paid_amount;
    }

    public void setPaid_amount(double paid_amount) {
        this.paid_amount = paid_amount;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Sales{" +
                "sale_id=" + sale_id +
                ", customer_id=" + customer_id +
                ", user_id=" + user_id +
                ", sale_date=" + sale_date +
                ", sub_total=" + sub_total +
                ", discount=" + discount +
                ", grand_total=" + grand_total +
                ", paid_amount=" + paid_amount +
                ", payment_status='" + payment_status + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
