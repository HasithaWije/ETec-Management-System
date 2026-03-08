package lk.ijse.etecmanagementsystem.entity;

import java.util.Date;

public class RepairJob {

    private int repair_id;
    private int cus_id;
    private int user_id;
    private String device_name;
    private String device_sn;
    private String problem_desc;
    private String diagnosis_desc;
    private String repair_results;
    private String status;
    private Date date_in;
    private Date date_out;
    private double labor_cost;
    private double parts_cost;
    private double discount;
    private double total_amount;
    private double paid_amount;
    private String payment_status;

    public RepairJob() {
    }

    public RepairJob(int repair_id, int cus_id, int user_id, String device_name, String device_sn, String problem_desc, String diagnosis_desc, String repair_results, String status, Date date_in, Date date_out, double labor_cost, double parts_cost, double discount, double total_amount, double paid_amount, String payment_status) {
        this.repair_id = repair_id;
        this.cus_id = cus_id;
        this.user_id = user_id;
        this.device_name = device_name;
        this.device_sn = device_sn;
        this.problem_desc = problem_desc;
        this.diagnosis_desc = diagnosis_desc;
        this.repair_results = repair_results;
        this.status = status;
        this.date_in = date_in;
        this.date_out = date_out;
        this.labor_cost = labor_cost;
        this.parts_cost = parts_cost;
        this.discount = discount;
        this.total_amount = total_amount;
        this.paid_amount = paid_amount;
        this.payment_status = payment_status;
    }
    public RepairJob(int cus_id, int userId, String device_name, String device_sn, String problem_desc, String status, Date date_in) {
        this.cus_id = cus_id;
        this.user_id = userId;
        this.device_name = device_name;
        this.device_sn = device_sn;
        this.problem_desc = problem_desc;
        this.status = status;
        this.date_in = date_in;
    }

    public RepairJob(int repairId, int customerId, String deviceName, String deviceSn, String problemDesc) {
        this.repair_id = repairId;
        this.cus_id = customerId;
        this.device_name = deviceName;
        this.device_sn = deviceSn;
        this.problem_desc = problemDesc;
    }

    public RepairJob(int repair_id, String problem_desc, String diagnosis_desc, String repair_results, double labor_cost, double parts_cost, double total_amount) {
        this.repair_id = repair_id;
        this.problem_desc = problem_desc;
        this.diagnosis_desc = diagnosis_desc;
        this.repair_results = repair_results;
        this.labor_cost = labor_cost;
        this.parts_cost = parts_cost;
        this.total_amount = total_amount;
    }

    public int getRepair_id() {
        return repair_id;
    }

    public void setRepair_id(int repair_id) {
        this.repair_id = repair_id;
    }

    public int getCus_id() {
        return cus_id;
    }

    public void setCus_id(int cus_id) {
        this.cus_id = cus_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
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

    public String getDiagnosis_desc() {
        return diagnosis_desc;
    }

    public void setDiagnosis_desc(String diagnosis_desc) {
        this.diagnosis_desc = diagnosis_desc;
    }

    public String getRepair_results() {
        return repair_results;
    }

    public void setRepair_results(String repair_results) {
        this.repair_results = repair_results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate_in() {
        return date_in;
    }

    public void setDate_in(Date date_in) {
        this.date_in = date_in;
    }

    public Date getDate_out() {
        return date_out;
    }

    public void setDate_out(Date date_out) {
        this.date_out = date_out;
    }

    public double getLabor_cost() {
        return labor_cost;
    }

    public void setLabor_cost(double labor_cost) {
        this.labor_cost = labor_cost;
    }

    public double getParts_cost() {
        return parts_cost;
    }

    public void setParts_cost(double parts_cost) {
        this.parts_cost = parts_cost;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
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

    @Override
    public String toString() {
        return "RepairJob{" +
                "repair_id=" + repair_id +
                ", cus_id=" + cus_id +
                ", user_id=" + user_id +
                ", device_name='" + device_name + '\'' +
                ", device_sn='" + device_sn + '\'' +
                ", problem_desc='" + problem_desc + '\'' +
                ", diagnosis_desc='" + diagnosis_desc + '\'' +
                ", repair_results='" + repair_results + '\'' +
                ", status='" + status + '\'' +
                ", date_in=" + date_in +
                ", date_out=" + date_out +
                ", labor_cost=" + labor_cost +
                ", parts_cost=" + parts_cost +
                ", discount=" + discount +
                ", total_amount=" + total_amount +
                ", paid_amount=" + paid_amount +
                ", payment_status='" + payment_status + '\'' +
                '}';
    }
}
