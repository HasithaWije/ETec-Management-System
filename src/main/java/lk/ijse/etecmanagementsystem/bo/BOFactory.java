package lk.ijse.etecmanagementsystem.bo;


import lk.ijse.etecmanagementsystem.bo.custom.impl.*;

public class BOFactory {
    private static BOFactory instance;

    private BOFactory() {}

    public static BOFactory getInstance() {
        if (instance == null) {
            instance = new BOFactory();
        }
        return instance;
    }

    public enum BOTypes {
        CATEGORY, CUSTOMER, DASHBOARD, INVENTORY, REPAIRS, REPORT, SALES
    }

    public SuperBO getBO(BOTypes boType) {
        switch (boType) {
            case CATEGORY:
                return new CategoryBOImpl();
            case CUSTOMER:
                return new CustomerBOImpl();
            case DASHBOARD:
                return new DashboardBOImpl();
            case INVENTORY:
                return new InventoryBOImpl();
            case REPAIRS:
                return new RepairsBOImpl();
            case REPORT:
                return new ReportBOImpl();
            case SALES:
                return new SalesBOImpl();
            default:
                return null;
        }
    }
}
