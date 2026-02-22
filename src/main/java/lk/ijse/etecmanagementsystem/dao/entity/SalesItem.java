package lk.ijse.etecmanagementsystem.dao.entity;

public class SalesItem {
    int sales_item_id;
    int sale_id;
    int item_id;
    int customer_warranty_months;
    double unit_price;
    double discount;

    public SalesItem() {
    }

    public SalesItem(int sales_item_id, int sale_id, int item_id, int customer_warranty_months, double unit_price, double discount) {
        this.sales_item_id = sales_item_id;
        this.sale_id = sale_id;
        this.item_id = item_id;
        this.customer_warranty_months = customer_warranty_months;
        this.unit_price = unit_price;
        this.discount = discount;
    }

    public int getSales_item_id() {
        return sales_item_id;
    }

    public void setSales_item_id(int sales_item_id) {
        this.sales_item_id = sales_item_id;
    }

    public int getSale_id() {
        return sale_id;
    }

    public void setSale_id(int sale_id) {
        this.sale_id = sale_id;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getCustomer_warranty_months() {
        return customer_warranty_months;
    }

    public void setCustomer_warranty_months(int customer_warranty_months) {
        this.customer_warranty_months = customer_warranty_months;
    }

    public double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(double unit_price) {
        this.unit_price = unit_price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}
