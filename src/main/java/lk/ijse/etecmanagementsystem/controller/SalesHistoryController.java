package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.etecmanagementsystem.dao.SalesDAOImpl;
import lk.ijse.etecmanagementsystem.dto.tm.SalesTM;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SalesHistoryController {

    @FXML
    private DatePicker dpFromDate;

    @FXML
    private DatePicker dpToDate;

    @FXML
    private TableView<SalesTM> tblSalesHistory;

    @FXML
    private TableColumn<SalesTM, Integer> colSaleId;

    @FXML
    private TableColumn<SalesTM, String> colCustomer;

    @FXML
    private TableColumn<SalesTM, String> colUser;

    @FXML
    private TableColumn<SalesTM, String> colDesc;

    @FXML
    private TableColumn<SalesTM, Double> colSubTotal;

    @FXML
    private TableColumn<SalesTM, Double> colDiscount;

    @FXML
    private TableColumn<SalesTM, Double> colGrandTotal;

    @FXML
    private TableColumn<SalesTM, Double> colPaid;

    SalesDAOImpl salesDAO = new SalesDAOImpl();

    @FXML
    public void initialize() {
        setCellValueFactory();
        loadAllSales();
    }

    private void setCellValueFactory() {
        colSaleId.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
    }

    private void loadAllSales() {
        try {
            List<SalesTM> salesList = salesDAO.getAllSales();
            ObservableList<SalesTM> obList = FXCollections.observableArrayList(salesList);
            tblSalesHistory.setItems(obList);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "SQL Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnFilterOnAction(ActionEvent event) {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();

        if (fromDate == null || toDate == null) {
            new Alert(Alert.AlertType.WARNING, "Please select both 'From' and 'To' dates.").show();
            return;
        }

        if (fromDate.isAfter(toDate)) {
            new Alert(Alert.AlertType.WARNING, "'From' date cannot be after 'To' date.").show();
            return;
        }

        try {
            List<SalesTM> filteredList = salesDAO.getSalesByDateRange(fromDate, toDate);
            tblSalesHistory.setItems(FXCollections.observableArrayList(filteredList));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error filtering data: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnRefreshOnAction(ActionEvent event) {
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        loadAllSales();
    }
}