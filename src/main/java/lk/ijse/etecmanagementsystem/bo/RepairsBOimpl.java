package lk.ijse.etecmanagementsystem.bo;

import lk.ijse.etecmanagementsystem.dao.CustomerDAOImpl;
import lk.ijse.etecmanagementsystem.dao.RepairJobDAOImpl;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;

import java.sql.SQLException;
import java.util.List;

public class RepairsBOimpl {
    RepairJobDAOImpl repairJobDAO = new RepairJobDAOImpl();

    public List<RepairJobDTO> getAllRepairJobs() throws SQLException {

        List<RepairJobDTO> repairJobs = repairJobDAO.getAllRepairJobs();
        return repairJobs;

    }
}
