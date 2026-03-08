package lk.ijse.etecmanagementsystem.dto;

import lk.ijse.etecmanagementsystem.util.PaymentStatus;
import lk.ijse.etecmanagementsystem.util.ProductCondition;
import lk.ijse.etecmanagementsystem.util.RepairStatus;
import lk.ijse.etecmanagementsystem.util.TransactionFlow;

import java.sql.Date;

public class CustomDTO {
    // CategoryDTO
    private String categoryName;
    private String categoryDescription;

    // CustomerDTO
    private int customerId;
    private String customerName;
    private String customerNumber;
    private String customerEmailAddress;
    private String customerAddress;

    // DashboardDTO
    private double todayIncome;
    private int activeRepairs;
    private int lowStock;
    private double pendingPayments;
    private int refId;
    private String type;
    private double due;

    private int urgentRepairId;
    private String urgentRepairDeviceName;
    private String urgentRepairStatus;
    private String urgentRepairDateIn;

    // InventoryItemDTO
    private int inventoryItemId;

    public CustomDTO(int inventoryItemId, String inventoryProductName, String inventorySerialNumber, ProductCondition inventoryProductCondition, int inventoryCustomerWarranty, double inventoryItemPrice) {
        this.inventoryItemId = inventoryItemId;
        this.inventoryProductName = inventoryProductName;
        this.inventorySerialNumber = inventorySerialNumber;
        this.inventoryProductCondition = inventoryProductCondition;
        this.inventoryCustomerWarranty = inventoryCustomerWarranty;
        this.inventoryItemPrice = inventoryItemPrice;
    }

    private String inventoryProductName;
    private String inventorySerialNumber;
    private ProductCondition inventoryProductCondition;
    private int inventoryCustomerWarranty;
    private double inventoryItemPrice;
    private String inventoryStatus;

    // ItemCartDTO
    private int cartItemId;
    private String cartItemName;
    private String cartSerialNo;
    private double cartUnitPrice;
    private double cartDiscount;
    private double cartTotal;

    // ProductDTO
    private String productId;
    private String productName;
    private String productDescription;
    private double productSellPrice;
    private String productCategory;
    private ProductCondition productCondition;
    private double productBuyPrice;
    private int productWarrantyMonth;
    private int productQty;
    private String productImagePath;

    public CustomDTO(int productItemId, int productItemStockId, int productItemSupplierId, String productItemSerialNumber, String productItemProductName, String productItemSupplierName, int productItemSupplierWarranty, int productItemCustomerWarranty, String productItemStatus, Date productItemAddedDate, Date productItemSoldDate) {
        this.productItemId = productItemId;
        this.productItemStockId = productItemStockId;
        this.productItemSupplierId = productItemSupplierId;
        this.productItemSerialNumber = productItemSerialNumber;
        this.productItemProductName = productItemProductName;
        this.productItemSupplierName = productItemSupplierName;
        this.productItemSupplierWarranty = productItemSupplierWarranty;
        this.productItemCustomerWarranty = productItemCustomerWarranty;
        this.productItemStatus = productItemStatus;
        this.productItemAddedDate = productItemAddedDate;
        this.productItemSoldDate = productItemSoldDate;
    }

    // ProductItemDTO
    private int productItemId;
    private int productItemStockId;
    private int productItemSupplierId;
    private String productItemSerialNumber;
    private String productItemProductName;
    private String productItemSupplierName;
    private int productItemSupplierWarranty;
    private int productItemCustomerWarranty;
    private String productItemStatus;
    private Date productItemAddedDate;
    private Date productItemSoldDate;

    // RepairItemDTO
    private int repairItemId;
    private int repairItemRepairId;
    private int repairItemItemId;
    private double repairItemUnitPrice;

    // RepairJobDTO
    private int repairJobId;
    private int repairJobCustomerId;
    private int repairJobUserId;
    private String repairJobDeviceName;
    private String repairJobDeviceSn;
    private String repairJobProblemDesc;
    private String repairJobDiagnosisDesc;
    private String repairJobRepairResults;
    private RepairStatus repairJobStatus;
    private java.util.Date repairJobDateIn;
    private java.util.Date repairJobDateOut;
    private double repairJobLaborCost;
    private double repairJobPartsCost;
    private double repairJobTotalAmount;
    private double repairJobPaidAmount;
    private double repairJobDiscount;
    private PaymentStatus repairJobPaymentStatus;

    // SalesDTO
    private int saleId;
    private int saleCustomerId;
    private int saleUserId;
    private int saleQty;
    private java.util.Date saleDate;
    private double saleSubtotal;
    private double saleDiscount;
    private double saleGrandTotal;
    private double salePaidAmount;
    private int saleCustomerWarrantyMonths;
    private PaymentStatus salePaymentStatus;
    private String saleDescription;

    // SupplierDTO
    private int supplierId;
    private String supplierName;
    private String supplierContactNumber;
    private String supplierEmailAddress;
    private String supplierAddress;

    // TopProductDTO
    private int topProductRank;
    private String topProductName;
    private double topProductPopularity;
    private String topProductSalesPercentageText;

    // TransactionDTO
    private String transactionId;
    private String transactionDate;
    private String transactionType;
    private String transactionPaymentMethod;
    private double transactionAmount;
    private TransactionFlow transactionFlow;
    private int transactionSaleId;
    private int transactionRepairId;
    private int transactionCustomerId;
    private int transactionUserId;
    private String transactionReferenceNote;

    // UserDTO
    private int userId;
    private String userFullName;
    private String userContact;
    private String userAddress;
    private String userEmail;
    private String userUsername;
    private String userPassword;
    private String userRole;

    //    ItemDeleteStatus
    private int realAvailableCount;
    private int restrictedCount;

    public CustomDTO() {
    }

    public CustomDTO(double todayIncome, int activeRepairs, int lowStock, double pendingPayments) {
        this.todayIncome = todayIncome;
        this.activeRepairs = activeRepairs;
        this.lowStock = lowStock;
        this.pendingPayments = pendingPayments;
    }

    public CustomDTO(int refId, String customerName, String type, double due) {
        this.refId = refId;
        this.customerName = customerName;
        this.type = type;
        this.due = due;
    }

    public CustomDTO(int urgentRepairId, String urgentRepairDeviceName, String urgentRepairStatus, String dateIn) {
        this.urgentRepairId = urgentRepairId;
        this.urgentRepairDeviceName = urgentRepairDeviceName;
        this.urgentRepairStatus = urgentRepairStatus;
        this.urgentRepairDateIn = dateIn;
    }

    public CustomDTO(int realAvailableCount, int restrictedCount) {
        this.realAvailableCount = realAvailableCount;
        this.restrictedCount = restrictedCount;
    }

    public int getRealAvailableCount() {
        return realAvailableCount;
    }

    public void setRealAvailableCount(int realAvailableCount) {
        this.realAvailableCount = realAvailableCount;
    }

    public int getRestrictedCount() {
        return restrictedCount;
    }

    public void setRestrictedCount(int restrictedCount) {
        this.restrictedCount = restrictedCount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getCustomerEmailAddress() {
        return customerEmailAddress;
    }

    public void setCustomerEmailAddress(String customerEmailAddress) {
        this.customerEmailAddress = customerEmailAddress;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public double getTodayIncome() {
        return todayIncome;
    }

    public void setTodayIncome(double todayIncome) {
        this.todayIncome = todayIncome;
    }

    public int getActiveRepairs() {
        return activeRepairs;
    }

    public void setActiveRepairs(int activeRepairs) {
        this.activeRepairs = activeRepairs;
    }

    public int getLowStock() {
        return lowStock;
    }

    public void setLowStock(int lowStock) {
        this.lowStock = lowStock;
    }

    public double getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(double pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public int getRefId() {
        return refId;
    }

    public void setRefId(int refId) {
        this.refId = refId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getDue() {
        return due;
    }

    public void setDue(double due) {
        this.due = due;
    }

    public int getUrgentRepairId() {
        return urgentRepairId;
    }

    public void setUrgentRepairId(int urgentRepairId) {
        this.urgentRepairId = urgentRepairId;
    }

    public String getUrgentRepairDeviceName() {
        return urgentRepairDeviceName;
    }

    public void setUrgentRepairDeviceName(String urgentRepairDeviceName) {
        this.urgentRepairDeviceName = urgentRepairDeviceName;
    }

    public String getUrgentRepairStatus() {
        return urgentRepairStatus;
    }

    public void setUrgentRepairStatus(String urgentRepairStatus) {
        this.urgentRepairStatus = urgentRepairStatus;
    }

    public String getUrgentRepairDateIn() {
        return urgentRepairDateIn;
    }

    public void setUrgentRepairDateIn(String urgentRepairDateIn) {
        this.urgentRepairDateIn = urgentRepairDateIn;
    }

    public int getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(int inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public String getInventoryProductName() {
        return inventoryProductName;
    }

    public void setInventoryProductName(String inventoryProductName) {
        this.inventoryProductName = inventoryProductName;
    }

    public String getInventorySerialNumber() {
        return inventorySerialNumber;
    }

    public void setInventorySerialNumber(String inventorySerialNumber) {
        this.inventorySerialNumber = inventorySerialNumber;
    }

    public ProductCondition getInventoryProductCondition() {
        return inventoryProductCondition;
    }

    public void setInventoryProductCondition(ProductCondition inventoryProductCondition) {
        this.inventoryProductCondition = inventoryProductCondition;
    }

    public int getInventoryCustomerWarranty() {
        return inventoryCustomerWarranty;
    }

    public void setInventoryCustomerWarranty(int inventoryCustomerWarranty) {
        this.inventoryCustomerWarranty = inventoryCustomerWarranty;
    }

    public double getInventoryItemPrice() {
        return inventoryItemPrice;
    }

    public void setInventoryItemPrice(double inventoryItemPrice) {
        this.inventoryItemPrice = inventoryItemPrice;
    }

    public String getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(String inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getCartItemName() {
        return cartItemName;
    }

    public void setCartItemName(String cartItemName) {
        this.cartItemName = cartItemName;
    }

    public String getCartSerialNo() {
        return cartSerialNo;
    }

    public void setCartSerialNo(String cartSerialNo) {
        this.cartSerialNo = cartSerialNo;
    }

    public double getCartUnitPrice() {
        return cartUnitPrice;
    }

    public void setCartUnitPrice(double cartUnitPrice) {
        this.cartUnitPrice = cartUnitPrice;
    }

    public double getCartDiscount() {
        return cartDiscount;
    }

    public void setCartDiscount(double cartDiscount) {
        this.cartDiscount = cartDiscount;
    }

    public double getCartTotal() {
        return cartTotal;
    }

    public void setCartTotal(double cartTotal) {
        this.cartTotal = cartTotal;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getProductSellPrice() {
        return productSellPrice;
    }

    public void setProductSellPrice(double productSellPrice) {
        this.productSellPrice = productSellPrice;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public ProductCondition getProductCondition() {
        return productCondition;
    }

    public void setProductCondition(ProductCondition productCondition) {
        this.productCondition = productCondition;
    }

    public double getProductBuyPrice() {
        return productBuyPrice;
    }

    public void setProductBuyPrice(double productBuyPrice) {
        this.productBuyPrice = productBuyPrice;
    }

    public int getProductWarrantyMonth() {
        return productWarrantyMonth;
    }

    public void setProductWarrantyMonth(int productWarrantyMonth) {
        this.productWarrantyMonth = productWarrantyMonth;
    }

    public int getProductQty() {
        return productQty;
    }

    public void setProductQty(int productQty) {
        this.productQty = productQty;
    }

    public String getProductImagePath() {
        return productImagePath;
    }

    public void setProductImagePath(String productImagePath) {
        this.productImagePath = productImagePath;
    }

    public int getProductItemId() {
        return productItemId;
    }

    public void setProductItemId(int productItemId) {
        this.productItemId = productItemId;
    }

    public int getProductItemStockId() {
        return productItemStockId;
    }

    public void setProductItemStockId(int productItemStockId) {
        this.productItemStockId = productItemStockId;
    }

    public int getProductItemSupplierId() {
        return productItemSupplierId;
    }

    public void setProductItemSupplierId(int productItemSupplierId) {
        this.productItemSupplierId = productItemSupplierId;
    }

    public String getProductItemSerialNumber() {
        return productItemSerialNumber;
    }

    public void setProductItemSerialNumber(String productItemSerialNumber) {
        this.productItemSerialNumber = productItemSerialNumber;
    }

    public String getProductItemProductName() {
        return productItemProductName;
    }

    public void setProductItemProductName(String productItemProductName) {
        this.productItemProductName = productItemProductName;
    }

    public String getProductItemSupplierName() {
        return productItemSupplierName;
    }

    public void setProductItemSupplierName(String productItemSupplierName) {
        this.productItemSupplierName = productItemSupplierName;
    }

    public int getProductItemSupplierWarranty() {
        return productItemSupplierWarranty;
    }

    public void setProductItemSupplierWarranty(int productItemSupplierWarranty) {
        this.productItemSupplierWarranty = productItemSupplierWarranty;
    }

    public int getProductItemCustomerWarranty() {
        return productItemCustomerWarranty;
    }

    public void setProductItemCustomerWarranty(int productItemCustomerWarranty) {
        this.productItemCustomerWarranty = productItemCustomerWarranty;
    }

    public String getProductItemStatus() {
        return productItemStatus;
    }

    public void setProductItemStatus(String productItemStatus) {
        this.productItemStatus = productItemStatus;
    }

    public Date getProductItemAddedDate() {
        return productItemAddedDate;
    }

    public void setProductItemAddedDate(Date productItemAddedDate) {
        this.productItemAddedDate = productItemAddedDate;
    }

    public Date getProductItemSoldDate() {
        return productItemSoldDate;
    }

    public void setProductItemSoldDate(Date productItemSoldDate) {
        this.productItemSoldDate = productItemSoldDate;
    }

    public int getRepairItemId() {
        return repairItemId;
    }

    public void setRepairItemId(int repairItemId) {
        this.repairItemId = repairItemId;
    }

    public int getRepairItemRepairId() {
        return repairItemRepairId;
    }

    public void setRepairItemRepairId(int repairItemRepairId) {
        this.repairItemRepairId = repairItemRepairId;
    }

    public int getRepairItemItemId() {
        return repairItemItemId;
    }

    public void setRepairItemItemId(int repairItemItemId) {
        this.repairItemItemId = repairItemItemId;
    }

    public double getRepairItemUnitPrice() {
        return repairItemUnitPrice;
    }

    public void setRepairItemUnitPrice(double repairItemUnitPrice) {
        this.repairItemUnitPrice = repairItemUnitPrice;
    }

    public int getRepairJobId() {
        return repairJobId;
    }

    public void setRepairJobId(int repairJobId) {
        this.repairJobId = repairJobId;
    }

    public int getRepairJobCustomerId() {
        return repairJobCustomerId;
    }

    public void setRepairJobCustomerId(int repairJobCustomerId) {
        this.repairJobCustomerId = repairJobCustomerId;
    }

    public int getRepairJobUserId() {
        return repairJobUserId;
    }

    public void setRepairJobUserId(int repairJobUserId) {
        this.repairJobUserId = repairJobUserId;
    }

    public String getRepairJobDeviceName() {
        return repairJobDeviceName;
    }

    public void setRepairJobDeviceName(String repairJobDeviceName) {
        this.repairJobDeviceName = repairJobDeviceName;
    }

    public String getRepairJobDeviceSn() {
        return repairJobDeviceSn;
    }

    public void setRepairJobDeviceSn(String repairJobDeviceSn) {
        this.repairJobDeviceSn = repairJobDeviceSn;
    }

    public String getRepairJobProblemDesc() {
        return repairJobProblemDesc;
    }

    public void setRepairJobProblemDesc(String repairJobProblemDesc) {
        this.repairJobProblemDesc = repairJobProblemDesc;
    }

    public String getRepairJobDiagnosisDesc() {
        return repairJobDiagnosisDesc;
    }

    public void setRepairJobDiagnosisDesc(String repairJobDiagnosisDesc) {
        this.repairJobDiagnosisDesc = repairJobDiagnosisDesc;
    }

    public String getRepairJobRepairResults() {
        return repairJobRepairResults;
    }

    public void setRepairJobRepairResults(String repairJobRepairResults) {
        this.repairJobRepairResults = repairJobRepairResults;
    }

    public RepairStatus getRepairJobStatus() {
        return repairJobStatus;
    }

    public void setRepairJobStatus(RepairStatus repairJobStatus) {
        this.repairJobStatus = repairJobStatus;
    }

    public java.util.Date getRepairJobDateIn() {
        return repairJobDateIn;
    }

    public void setRepairJobDateIn(java.util.Date repairJobDateIn) {
        this.repairJobDateIn = repairJobDateIn;
    }

    public java.util.Date getRepairJobDateOut() {
        return repairJobDateOut;
    }

    public void setRepairJobDateOut(java.util.Date repairJobDateOut) {
        this.repairJobDateOut = repairJobDateOut;
    }

    public double getRepairJobLaborCost() {
        return repairJobLaborCost;
    }

    public void setRepairJobLaborCost(double repairJobLaborCost) {
        this.repairJobLaborCost = repairJobLaborCost;
    }

    public double getRepairJobPartsCost() {
        return repairJobPartsCost;
    }

    public void setRepairJobPartsCost(double repairJobPartsCost) {
        this.repairJobPartsCost = repairJobPartsCost;
    }

    public double getRepairJobTotalAmount() {
        return repairJobTotalAmount;
    }

    public void setRepairJobTotalAmount(double repairJobTotalAmount) {
        this.repairJobTotalAmount = repairJobTotalAmount;
    }

    public double getRepairJobPaidAmount() {
        return repairJobPaidAmount;
    }

    public void setRepairJobPaidAmount(double repairJobPaidAmount) {
        this.repairJobPaidAmount = repairJobPaidAmount;
    }

    public double getRepairJobDiscount() {
        return repairJobDiscount;
    }

    public void setRepairJobDiscount(double repairJobDiscount) {
        this.repairJobDiscount = repairJobDiscount;
    }

    public PaymentStatus getRepairJobPaymentStatus() {
        return repairJobPaymentStatus;
    }

    public void setRepairJobPaymentStatus(PaymentStatus repairJobPaymentStatus) {
        this.repairJobPaymentStatus = repairJobPaymentStatus;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getSaleCustomerId() {
        return saleCustomerId;
    }

    public void setSaleCustomerId(int saleCustomerId) {
        this.saleCustomerId = saleCustomerId;
    }

    public int getSaleUserId() {
        return saleUserId;
    }

    public void setSaleUserId(int saleUserId) {
        this.saleUserId = saleUserId;
    }

    public int getSaleQty() {
        return saleQty;
    }

    public void setSaleQty(int saleQty) {
        this.saleQty = saleQty;
    }

    public java.util.Date getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(java.util.Date saleDate) {
        this.saleDate = saleDate;
    }

    public double getSaleSubtotal() {
        return saleSubtotal;
    }

    public void setSaleSubtotal(double saleSubtotal) {
        this.saleSubtotal = saleSubtotal;
    }

    public double getSaleDiscount() {
        return saleDiscount;
    }

    public void setSaleDiscount(double saleDiscount) {
        this.saleDiscount = saleDiscount;
    }

    public double getSaleGrandTotal() {
        return saleGrandTotal;
    }

    public void setSaleGrandTotal(double saleGrandTotal) {
        this.saleGrandTotal = saleGrandTotal;
    }

    public double getSalePaidAmount() {
        return salePaidAmount;
    }

    public void setSalePaidAmount(double salePaidAmount) {
        this.salePaidAmount = salePaidAmount;
    }

    public int getSaleCustomerWarrantyMonths() {
        return saleCustomerWarrantyMonths;
    }

    public void setSaleCustomerWarrantyMonths(int saleCustomerWarrantyMonths) {
        this.saleCustomerWarrantyMonths = saleCustomerWarrantyMonths;
    }

    public PaymentStatus getSalePaymentStatus() {
        return salePaymentStatus;
    }

    public void setSalePaymentStatus(PaymentStatus salePaymentStatus) {
        this.salePaymentStatus = salePaymentStatus;
    }

    public String getSaleDescription() {
        return saleDescription;
    }

    public void setSaleDescription(String saleDescription) {
        this.saleDescription = saleDescription;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierContactNumber() {
        return supplierContactNumber;
    }

    public void setSupplierContactNumber(String supplierContactNumber) {
        this.supplierContactNumber = supplierContactNumber;
    }

    public String getSupplierEmailAddress() {
        return supplierEmailAddress;
    }

    public void setSupplierEmailAddress(String supplierEmailAddress) {
        this.supplierEmailAddress = supplierEmailAddress;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    public int getTopProductRank() {
        return topProductRank;
    }

    public void setTopProductRank(int topProductRank) {
        this.topProductRank = topProductRank;
    }

    public String getTopProductName() {
        return topProductName;
    }

    public void setTopProductName(String topProductName) {
        this.topProductName = topProductName;
    }

    public double getTopProductPopularity() {
        return topProductPopularity;
    }

    public void setTopProductPopularity(double topProductPopularity) {
        this.topProductPopularity = topProductPopularity;
    }

    public String getTopProductSalesPercentageText() {
        return topProductSalesPercentageText;
    }

    public void setTopProductSalesPercentageText(String topProductSalesPercentageText) {
        this.topProductSalesPercentageText = topProductSalesPercentageText;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionPaymentMethod() {
        return transactionPaymentMethod;
    }

    public void setTransactionPaymentMethod(String transactionPaymentMethod) {
        this.transactionPaymentMethod = transactionPaymentMethod;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public TransactionFlow getTransactionFlow() {
        return transactionFlow;
    }

    public void setTransactionFlow(TransactionFlow transactionFlow) {
        this.transactionFlow = transactionFlow;
    }

    public int getTransactionSaleId() {
        return transactionSaleId;
    }

    public void setTransactionSaleId(int transactionSaleId) {
        this.transactionSaleId = transactionSaleId;
    }

    public int getTransactionRepairId() {
        return transactionRepairId;
    }

    public void setTransactionRepairId(int transactionRepairId) {
        this.transactionRepairId = transactionRepairId;
    }

    public int getTransactionCustomerId() {
        return transactionCustomerId;
    }

    public void setTransactionCustomerId(int transactionCustomerId) {
        this.transactionCustomerId = transactionCustomerId;
    }

    public int getTransactionUserId() {
        return transactionUserId;
    }

    public void setTransactionUserId(int transactionUserId) {
        this.transactionUserId = transactionUserId;
    }

    public String getTransactionReferenceNote() {
        return transactionReferenceNote;
    }

    public void setTransactionReferenceNote(String transactionReferenceNote) {
        this.transactionReferenceNote = transactionReferenceNote;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserContact() {
        return userContact;
    }

    public void setUserContact(String userContact) {
        this.userContact = userContact;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

}
