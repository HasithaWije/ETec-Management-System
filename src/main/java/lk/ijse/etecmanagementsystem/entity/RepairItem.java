package lk.ijse.etecmanagementsystem.entity;

public class RepairItem {
    private int id;
    private int repair_id;
    private int item_id;
    private double unit_price;

    public RepairItem() {
    }

    public RepairItem(int id, int repair_id, int item_id, double unit_price) {
        this.id = id;
        this.repair_id = repair_id;
        this.item_id = item_id;
        this.unit_price = unit_price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRepair_id() {
        return repair_id;
    }

    public void setRepair_id(int repair_id) {
        this.repair_id = repair_id;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(double unit_price) {
        this.unit_price = unit_price;
    }

    @Override
    public String toString() {
        return "RepairItem{" +
                "id=" + id +
                ", repair_id=" + repair_id +
                ", item_id=" + item_id +
                ", unit_price=" + unit_price +
                '}';
    }
}
