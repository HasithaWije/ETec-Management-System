DROP DATABASE IF EXISTS ETec;
CREATE DATABASE IF NOT EXISTS ETec;
USE ETec;

CREATE TABLE User
(
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    contact   VARCHAR(20) NOT NULL ,
    address   VARCHAR(255) NOT NULL ,
    email     VARCHAR(100) NOT NULL UNIQUE,
    user_name VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    role      VARCHAR(50)  NOT NULL
);

CREATE TABLE Category
(
    category_name VARCHAR(50) PRIMARY KEY NOT NULL,
    description   VARCHAR(255)
);

CREATE TABLE Product
(
    stock_id        INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL UNIQUE,
    description     VARCHAR(500),
    category        VARCHAR(50)  NOT NULL,
    p_condition     VARCHAR(50),
    qty             INT          NOT NULL,
    warranty_months INT,
    image_path      VARCHAR(200),
    buy_price       DECIMAL(10, 2),
    sell_price      DECIMAL(10, 2),

    FOREIGN KEY (category) REFERENCES Category (category_name)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Supplier
(
    supplier_id    INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name  VARCHAR(150) NOT NULL,
    contact_number VARCHAR(20),
    email          VARCHAR(100),
    address        VARCHAR(255)
);

CREATE TABLE Customer
(
    cus_id  INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(150) NOT NULL,
    number  VARCHAR(20),
    email   VARCHAR(100),
    address VARCHAR(255)
);

CREATE TABLE ProductItem
(
    item_id              INT AUTO_INCREMENT PRIMARY KEY,
    stock_id             INT          NOT NULL,
    supplier_id          INT          NULL,
    serial_number        VARCHAR(100) UNIQUE,

    status               ENUM ('AVAILABLE', 'SOLD', 'RMA', 'RETURNED_TO_SUPPLIER','IN_REPAIR_USE', 'DAMAGED') DEFAULT 'AVAILABLE',

    added_date           DATETIME                                                             DEFAULT CURRENT_TIMESTAMP,
    supplier_warranty_mo INT                                                                  DEFAULT 0,

    sold_date            DATETIME                                                             DEFAULT NULL,
    customer_warranty_mo INT                                                                  DEFAULT 0,


    FOREIGN KEY (stock_id) REFERENCES Product (stock_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (supplier_id) REFERENCES Supplier (supplier_id) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE Sales
(
    sale_id                  INT AUTO_INCREMENT PRIMARY KEY,
    customer_id              INT,
    user_id                  INT      NOT NULL,
    sale_date                DATETIME NOT NULL,
    sub_total        DECIMAL(10, 2) NOT NULL,
    discount         DECIMAL(10, 2) DEFAULT 0.00,
    grand_total             DECIMAL(10, 2) NOT NULL,
    paid_amount     DECIMAL(10, 2) DEFAULT 0.00,
    payment_status          ENUM ('PENDING', 'PARTIAL', 'PAID') DEFAULT 'PENDING',
    description              VARCHAR(500),


    FOREIGN KEY (customer_id) REFERENCES Customer (cus_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    FOREIGN KEY (user_id) REFERENCES User (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE

);

CREATE TABLE SalesItem
(
    sales_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id       INT            NOT NULL,
    item_id       INT            NOT NULL,
    customer_warranty_months INT,
    unit_price        DECIMAL(10, 2) NOT NULL,
    discount         DECIMAL(10, 2) DEFAULT 0.00,

    FOREIGN KEY (sale_id) REFERENCES Sales (sale_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    FOREIGN KEY (item_id) REFERENCES ProductItem (item_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE RepairJob
(
    repair_id      INT AUTO_INCREMENT PRIMARY KEY,
    cus_id         INT          NOT NULL,
    user_id        INT          NOT NULL,

    -- Device Information
    device_name    VARCHAR(150) NOT NULL,
    device_sn      VARCHAR(100),
    problem_desc   TEXT         NOT NULL,
    diagnosis_desc TEXT,
    repair_results TEXT,

    -- Status Workflow
    status         ENUM ('PENDING', 'DIAGNOSIS', 'WAITING_PARTS', 'COMPLETED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',

    -- Dates
    date_in        DATETIME                                                                              DEFAULT CURRENT_TIMESTAMP,
    date_out       DATETIME     NULL,

    -- Financials (Derived from Diagram)
    labor_cost     DECIMAL(10, 2)                                                                        DEFAULT 0.00,
    parts_cost     DECIMAL(10, 2)                                                                        DEFAULT 0.00,
    discount       DECIMAL(10, 2)                                                                        DEFAULT 0.00,
    total_amount   DECIMAL(10, 2)                                                                        DEFAULT 0.00,
    paid_amount    DECIMAL(10, 2)                                                                        DEFAULT 0.00,

    -- Payment Flag (Like in Sales, actual money goes to TransactionRecord)
    payment_status ENUM ('PENDING', 'PARTIAL', 'PAID')                                                   DEFAULT 'PENDING',

    -- Constraints
    FOREIGN KEY (cus_id) REFERENCES Customer (cus_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    FOREIGN KEY (user_id) REFERENCES User (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE RepairSale
(
    repair_sale_id INT AUTO_INCREMENT PRIMARY KEY,
    repair_id      INT            NOT NULL,
    sale_id        INT            NOT NULL ,

    FOREIGN KEY (repair_id) REFERENCES RepairJob (repair_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    FOREIGN KEY (sale_id) REFERENCES Sales (sale_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE RepairItem
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    repair_id INT NOT NULL,  -- Links to the Job
    item_id   INT NOT NULL,  -- Links to the specific Part (SN-123)
    unit_price        DECIMAL(10, 2) NOT NULL,

    FOREIGN KEY (repair_id) REFERENCES RepairJob (repair_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    FOREIGN KEY (item_id) REFERENCES ProductItem (item_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE TransactionRecord
(
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,

    -- 'SALE_PAYMENT', 'REPAIR_PAYMENT', 'SUPPLIER_PAYMENT', 'REFUND', 'EXPENSE'
    transaction_type VARCHAR(50)        NOT NULL,

    payment_method   VARCHAR(50)        NOT NULL, -- 'CASH', 'CARD', 'TRANSFER'


    amount           DECIMAL(10, 2)     NOT NULL, -- Always positive
    flow             ENUM ('IN', 'OUT') NOT NULL, -- 'IN' for Income, 'OUT' for Expenses/Refunds

    -- Links (Nullable because a transaction might just be an electricity bill expense)
    sale_id          INT                NULL,
    repair_id        INT                NULL,
    customer_id      INT                NULL,

    user_id          INT                NOT NULL, -- Who processed this money?

    reference_note   VARCHAR(255),                -- "Check #1234" or "Refund for screen flicker"

    FOREIGN KEY (sale_id) REFERENCES Sales (sale_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    FOREIGN KEY (repair_id) REFERENCES RepairJob (repair_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    FOREIGN KEY (customer_id) REFERENCES Customer (cus_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    FOREIGN KEY (user_id) REFERENCES User (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);


INSERT INTO User (name, contact, address, email, user_name, password, role)
VALUES ('Admin User', '0771234567', '1, Admin Road, City', 'hasithawijesinghe8@gmail.com', 'admin', 'admin', 'ADMIN'),
       ('John Doe', '0759876543', '2, Staff Lane, City', 'hasithalakshanwijesinghe@gmail.com', 'johndoe', 'password123', 'STAFF'),
       ('Jane Smith', '0765432198', '3, Tech Street, City', '2', 'janesmith', 'securepass', 'TECHNICIAN');

INSERT INTO Category (category_name, description)
VALUES ('Laptops', 'Portable computers suitable for mobile use.'),
       ('Desktops', 'Personal computers designed for regular use at a single location.'),
       ('Monitors', 'Display screens for computers.'),
       ('Printers', 'Devices that produce physical copies of digital documents.'),
       ('Accessories', 'Peripheral devices and accessories for computers.');

INSERT INTO Supplier (supplier_name, contact_number, email, address)
VALUES ('Tech Supplies Co.', '123-456-7890', '', '123 Tech Street, Silicon Valley, CA'),
       ('Gadget World', '987-654-3210', '', '456 Gadget Avenue, Tech City, NY'),
       ('Computer Parts Inc.', '555-123-4567', '', '789 Computer Blvd, Hardware Town, TX');

-- net start MySQL80

SELECT COUNT(item_id) AS count  FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%';

SELECT pi.item_id, p.name, pi.serial_number, p.p_condition, p.sell_price FROM ProductItem pi JOIN Product p ON pi.stock_id = p.stock_id WHERE pi.status = 'AVAILABLE';