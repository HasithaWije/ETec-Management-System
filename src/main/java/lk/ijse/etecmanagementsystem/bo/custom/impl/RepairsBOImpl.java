package lk.ijse.etecmanagementsystem.bo.custom.impl;

import lk.ijse.etecmanagementsystem.bo.custom.RepairsBO;
import lk.ijse.etecmanagementsystem.dao.DAOFactory;
import lk.ijse.etecmanagementsystem.dao.custom.*;
import lk.ijse.etecmanagementsystem.dao.custom.impl.*;
import lk.ijse.etecmanagementsystem.dto.CustomDTO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.entity.RepairItem;
import lk.ijse.etecmanagementsystem.entity.RepairJob;
import lk.ijse.etecmanagementsystem.entity.Sales;
import lk.ijse.etecmanagementsystem.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.dto.PaymentStatus;
import lk.ijse.etecmanagementsystem.dto.RepairStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static lk.ijse.etecmanagementsystem.controller.RepairDashboardController.getRepairPartTMS;

public class RepairsBOImpl implements RepairsBO {
    RepairJobDAO repairJobDAO = (RepairJobDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.REPAIR_JOB);
    RepairItemDAO repairItemDAO = (RepairItemDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.REPAIR_ITEM);
    ProductItemDAO productItemDAO = (ProductItemDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.PRODUCT_ITEM);
    SalesDAO salesDAO = (SalesDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.SALES);
    QueryDAO queryDAO = (QueryDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
    TransactionRecordDAO transactionRecordDAO = (TransactionRecordDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.TRANSACTION_RECORD);
    ProductDAO productDAO = (ProductDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.PRODUCT);


    @Override
    public List<RepairJobDTO> getAllRepairJobs() throws SQLException {

        List<RepairJob> entities = repairJobDAO.getAll();
        List<RepairJobDTO> repairJobs = new java.util.ArrayList<>();

        for (RepairJob entity : entities) {
            repairJobs.add(new RepairJobDTO(
                    entity.getRepair_id(),
                    entity.getCus_id(),
                    entity.getUser_id(),
                    entity.getDevice_name(),
                    entity.getDevice_sn(),
                    entity.getProblem_desc(),
                    entity.getDiagnosis_desc(),
                    entity.getRepair_results(),
                    RepairStatus.valueOf(entity.getStatus()),
                    entity.getDate_in(),
                    entity.getDate_out(),
                    entity.getLabor_cost(),
                    entity.getParts_cost(),
                    entity.getTotal_amount(),
                    entity.getPaid_amount(),
                    entity.getDiscount(),
                    PaymentStatus.valueOf(entity.getPayment_status())
            ));
        }
        return repairJobs;

    }

    @Override
    public boolean updateRepairJobDetails(int repairId, String intake, String diagnosis, String resolution,
                                          double laborCost, double partsCost, double totalAmount,
                                          List<ProductItemDTO> activeParts,
                                          List<ProductItemDTO> returnedParts) throws SQLException {


        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // START TRANSACTION

            boolean jobUpdated = repairJobDAO.updateRepairCosts(new RepairJob(
                    repairId, intake, diagnosis, resolution,
                    laborCost, partsCost, totalAmount
            ));
            if (!jobUpdated) {
                connection.rollback();
                System.out.println("Failed to update repair job details 1");
                return false;
            }

            int repairItemId;
            for (ProductItemDTO part : activeParts) {
                repairItemId = repairItemDAO.getRepairItemId(repairId, part.getItemId());

                if (repairItemId == -1) {

                    boolean linkSaved = repairItemDAO.save(new RepairItem(
                            0,
                            repairId,
                            part.getItemId(),
                            part.getPrice()
                    ));

                    if (!linkSaved) {
                        connection.rollback();
                        System.out.println("Failed to update repair job details 2");
                        return false;
                    }
                    boolean marked = productItemDAO.updateItemForRepair(part.getItemId());
                    if (!marked) {
                        connection.rollback();
                        System.out.println("Failed to update repair job details 3");
                        return false;
                    }

                    boolean snFixed = productItemDAO.fixSerialForRepair(part.getItemId());
//                    if (!snFixed) {
//                        connection.rollback();
//                        System.out.println("Failed to update repair job details 4");
//
//                        return false;
//                    }

                    int stockId = getProductItem(part.getItemId()).getStockId();
                    if (stockId <= 0) {
                        connection.rollback();
                        System.out.println("Failed to update repair job details 5");

                        return false;
                    }
                    boolean qtyDecreased = productDAO.updateQty(stockId, -1);
                    if (!qtyDecreased) {
                        connection.rollback();
                        System.out.println("Failed to update repair job details 6");

                        return false;
                    }


                }
            }

            for (ProductItemDTO part : returnedParts) {

                boolean linkDeleted = repairItemDAO.deleteRepairItem(repairId, part.getItemId());
                if (!linkDeleted) {
                    connection.rollback();
                    System.out.println("Failed to update repair job details 7");

                    return false;
                }


                boolean snReplaced = productItemDAO.replaceSerialForReturned(part.getItemId());
//                if (!snReplaced) {
//                    connection.rollback();
//                    System.out.println("Failed to update repair job details 8");
//
//                    return false;
//                }

                boolean restocked = productItemDAO.updateItemAvailability(part.getItemId());
                if (!restocked) {
                    connection.rollback();
                    System.out.println("Failed to update repair job details 8");

                    return false;
                }

                int stockId = getProductItem(part.getItemId()).getStockId();
                if (stockId <= 0) {
                    connection.rollback();
                    System.out.println("Failed to update repair job details 10");

                    return false;
                }

                boolean qtyIncrease = productDAO.updateQty(stockId, 1);
                if (!qtyIncrease) {
                    connection.rollback();
                    System.out.println("Failed to update repair job details 11");

                    return false;
                }
            }

            connection.commit(); // COMMIT
            return true;

        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
        }
    }

    @Override
    public boolean completeCheckout(int repairId, int customerId, int userId,
                                    double totalAmount, double discount, double partsTotal, double paidAmount, String paymentMethod, String serialNumber) throws SQLException {


        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // START TRANSACTION

            String payStatus = "PENDING";
            if (paidAmount >= totalAmount) {
                payStatus = "PAID";
            } else if (paidAmount > 0) {
                payStatus = "PARTIAL";
            }

            boolean isUpdated = repairJobDAO.updateRepairPayment(paidAmount, totalAmount, discount, payStatus, repairId);
            if (!isUpdated) {
                connection.rollback();
                System.out.println("Failed to update repair payment");
                return false;
            }

            int saleId = -1;

            boolean hasParts = repairItemDAO.search(repairId) != null;
            System.out.println("Repair ID " + repairId + " has parts: " + hasParts);

            if (hasParts) {
                List<CustomDTO> dbParts = getUsedParts(repairId);
                List<RepairPartTM> partsToMark = getRepairPartTMS(dbParts);

                for (RepairPartTM repairPart : partsToMark) {
                    String sn = getProductItem(repairPart.getItemId()).getSerialNumber();
                    boolean marked = productItemDAO.updateStatus(sn, "SOLD");
                    if (!marked) {
                        connection.rollback();
                        System.out.println("Failed to mark part as sold: " + sn);
                        return false;
                    }
                }

                PaymentStatus paymentStatus = payStatus.equals("PAID") ? PaymentStatus.PAID : payStatus.equals("PARTIAL") ? PaymentStatus.PARTIAL : PaymentStatus.PENDING;
                boolean saleSaved = salesDAO.save(new Sales(
                        0,
                        customerId,
                        userId,
                        null,
                        partsTotal,
                        0,
                        partsTotal,
                        partsTotal,
                        paymentStatus.getLabel(),
                        "Repair Job Checkout - Job #" + repairId
                ));
                if (!saleSaved) {
                    connection.rollback();
                    return false;
                }

                int generatedSaleId = salesDAO.getLastInsertedSalesId();

                System.out.println("Generated Sale ID: " + generatedSaleId);

                if (generatedSaleId > 0) {
                    saleId = generatedSaleId;
                } else {
                    connection.rollback();
                    return false;
                }

                boolean linkSaved = new RepairSalesDAOImpl().saveRepairSale(repairId, saleId);
                if (!linkSaved) {
                    connection.rollback();
                    return false;
                }
            }

            boolean transactionRecorded = transactionRecordDAO.save(new TransactionRecord(
                    "REPAIR_PAYMENT",
                    paymentMethod,
                    paidAmount,
                    "IN",
                    repairId,
                    userId,
                    customerId,
                    "Repair Checkout Payment - Job #" + repairId
            ));
            if (!transactionRecorded) {
                connection.rollback();
                return false;
            }

            boolean jobUpdated = repairJobDAO.updateDateOut(payStatus, repairId);
            if (!jobUpdated) {
                connection.rollback();
                return false;
            }

            connection.commit(); // COMMIT TRANSACTION
            return true;

        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            e.printStackTrace();
            throw e;
        } catch (Exception ex) {
            if (connection != null) connection.rollback();
            ex.printStackTrace();
            throw new SQLException("Failed to complete checkout: " + ex.getMessage());
        } finally {
            if (connection != null) connection.setAutoCommit(true);
        }
    }

    @Override
    public ProductItemDTO getProductItem(int itemId) throws SQLException {
        CustomDTO customDTO = queryDAO.getProductItem(itemId);
        if (customDTO == null) {
            return null;
        }
        return new ProductItemDTO(
                customDTO.getProductItemId(),
                customDTO.getProductItemStockId(),
                customDTO.getProductItemSupplierId(),
                customDTO.getProductItemSerialNumber(),
                customDTO.getProductItemProductName(),
                customDTO.getProductItemSupplierName(),
                customDTO.getProductItemSupplierWarranty(),
                customDTO.getProductItemCustomerWarranty(),
                customDTO.getProductItemStatus(),
                customDTO.getProductItemAddedDate(),
                customDTO.getProductItemSoldDate()
        );
    }

    @Override
    public boolean saveRepairJob(RepairJobDTO repairJobDTO) throws SQLException {
        return repairJobDAO.save(new RepairJob(
                repairJobDTO.getCusId(),
                repairJobDTO.getUserId(),
                repairJobDTO.getDeviceName(),
                repairJobDTO.getDeviceSn(),
                repairJobDTO.getProblemDesc(),
                RepairStatus.PENDING.getLabel(),
                new Date()
        ));
    }

    @Override
    public int getLastInsertedRepairId() throws SQLException {
        return repairJobDAO.getLastInsertedRepairId();
    }

    @Override
    public boolean updateRepairJob(RepairJobDTO repairJobDTO) throws SQLException {
        return repairJobDAO.updateRepairJob(new RepairJob(

                repairJobDTO.getRepairId(),
                repairJobDTO.getCusId(),
                repairJobDTO.getDeviceName(),
                repairJobDTO.getDeviceSn(),
                repairJobDTO.getProblemDesc()

        ));
    }

    @Override
    public boolean updateStatus(int repairId, RepairStatus newStatus) throws SQLException {
        return repairJobDAO.updateStatus(repairId, newStatus);
    }

    @Override
    public boolean deleteRepairJob(int repairId) throws SQLException {
        return repairJobDAO.delete(repairId);
    }

    @Override
    public List<CustomDTO> getPendingRepairs() throws SQLException {
        return queryDAO.getPendingRepairs();
    }

    @Override
    public List<CustomDTO> getUsedParts(int repairId) throws SQLException {
        return queryDAO.getUsedParts(repairId);
    }

}
