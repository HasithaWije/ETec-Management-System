package lk.ijse.etecmanagementsystem.bo;


import lk.ijse.etecmanagementsystem.bo.custom.impl.CategoryBOImpl;
import lk.ijse.etecmanagementsystem.bo.custom.impl.CustomerBOImpl;

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
        CATEGORY, CUSTOMER
    }

    public SuperBO getBO(BOTypes boType) {
        switch (boType) {
            case CATEGORY:
                return new CategoryBOImpl();
            case CUSTOMER:
                return new CustomerBOImpl();
            default:
                return null;
        }
    }
}
