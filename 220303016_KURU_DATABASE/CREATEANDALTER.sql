CREATE DATABASE GrainStorageDB;

USE GrainStorageDB;


CREATE TABLE BRANCH (
branch_id INT PRIMARY KEY IDENTITY(1,1),
code VARCHAR(50) NOT NULL UNIQUE,
name VARCHAR(50) NOT NULL,
address VARCHAR(200),
city VARCHAR(50),
region VARCHAR(50),
phone VARCHAR(30),
branch_total_capacity DECIMAL(12,3)
);

CREATE TABLE SILO (
silo_id INT PRIMARY KEY IDENTITY(1,1),
branch_id INT NOT NULL,
code VARCHAR(50) NOT NULL UNIQUE,
location_code VARCHAR(50),
capacity_kg DECIMAL(12,3) NOT NULL,
silo_total_occupancy DECIMAL(12,2)
);

CREATE TABLE SILO_COMPARTMENT (
compartment_id INT PRIMARY KEY IDENTITY(1,1),
silo_id INT NOT NULL,
code VARCHAR(50) NOT NULL UNIQUE,
capacity_kg DECIMAL(12,3) NOT NULL
);

CREATE TABLE SUPPLIER (
supplier_id INT PRIMARY KEY IDENTITY(1,1),
name VARCHAR(50) NOT NULL,
type VARCHAR(50),
tax_no VARCHAR(50) UNIQUE,
average_quality_index DECIMAL(5,2)
);

CREATE TABLE PRODUCT (
product_id INT PRIMARY KEY IDENTITY(1,1),
name VARCHAR(50) NOT NULL,
grain_class VARCHAR(50) NOT NULL,
unit VARCHAR(30),
optimal_storage_temp DECIMAL(5,2),
shelf_life_days INT
);

CREATE TABLE INTAKE (
intake_id INT PRIMARY KEY IDENTITY(1,1),
supplier_id INT NOT NULL,
branch_id INT NOT NULL,
intake_datetime DATETIME NOT NULL,
gross_kg DECIMAL(12,3) NOT NULL,
tare_kg DECIMAL(12,3) NOT NULL,
net_kg AS (gross_kg - tare_kg)
);

CREATE TABLE LOT (
lot_id INT PRIMARY KEY,
product_id INT NOT NULL,
lot_code VARCHAR(50) NOT NULL UNIQUE,
initial_kg DECIMAL(12,3) NOT NULL,
status VARCHAR(30),
quality_grade VARCHAR(50),
spoilage_risk DECIMAL(5,2)
);

CREATE TABLE QUALITYTEST (
test_id INT PRIMARY KEY IDENTITY(1,1),
lot_id INT NOT NULL,
test_datetime DATETIME NOT NULL,
moisture DECIMAL(5,2),
protein DECIMAL(5,2),
foreign_matter DECIMAL(5,2),
overall_quality_index DECIMAL(5,2)
);

CREATE TABLE MOVEMENT (
movement_id INT PRIMARY KEY IDENTITY(1,1),
lot_id INT NOT NULL,
from_compartment_id INT NULL,
to_compartment_id INT NULL,
quantity_kg DECIMAL(12,3) NOT NULL,
movement_datetime DATETIME NOT NULL
);

CREATE TABLE CUSTOMER (
customer_id INT PRIMARY KEY IDENTITY(1,1),
name VARCHAR(50) NOT NULL,
tax_no VARCHAR(50) UNIQUE,
contact_info VARCHAR(200)
);

CREATE TABLE SALESORDER (
so_id INT PRIMARY KEY IDENTITY(1,1),
customer_id INT NOT NULL,
branch_id INT NOT NULL,
order_date DATETIME NOT NULL,
status VARCHAR(30)
);

CREATE TABLE SALESORDERLINE (
sol_id INT PRIMARY KEY IDENTITY(1,1),
so_id INT NOT NULL,
product_id INT NOT NULL,
quantity_kg DECIMAL(12,3) NOT NULL,
price_per_kg DECIMAL(12,3) NOT NULL
);

CREATE TABLE ALLOCATION (
allocation_id INT PRIMARY KEY IDENTITY(1,1),
sol_id INT NOT NULL,
lot_id INT NOT NULL,
allocated_kg DECIMAL(12,3) NOT NULL
);

CREATE UNIQUE INDEX UQ_Allocation_Sol_Lot ON ALLOCATION (sol_id, lot_id);


---------------------------------------------------------------------------------------


ALTER TABLE SILO
ADD CONSTRAINT FK_SILO_BRANCH
FOREIGN KEY (branch_id) REFERENCES BRANCH(branch_id);


ALTER TABLE SILO_COMPARTMENT
ADD CONSTRAINT FK_COMPARTMENT_SILO
FOREIGN KEY (silo_id) REFERENCES SILO(silo_id);


ALTER TABLE INTAKE
ADD CONSTRAINT FK_INTAKE_SUPPLIER
FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(supplier_id);


ALTER TABLE INTAKE
ADD CONSTRAINT FK_INTAKE_BRANCH
FOREIGN KEY (branch_id) REFERENCES BRANCH(branch_id);


ALTER TABLE LOT
ADD CONSTRAINT FK_LOT_INTAKE
FOREIGN KEY (lot_id) REFERENCES INTAKE(intake_id);


ALTER TABLE LOT
ADD CONSTRAINT FK_LOT_PRODUCT
FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id);


ALTER TABLE QUALITYTEST
ADD CONSTRAINT FK_QUALITYTEST_LOT
FOREIGN KEY (lot_id) REFERENCES LOT(lot_id);


ALTER TABLE MOVEMENT
ADD CONSTRAINT FK_MOVEMENT_LOT
FOREIGN KEY (lot_id) REFERENCES LOT(lot_id);


ALTER TABLE MOVEMENT
ADD CONSTRAINT FK_MOVEMENT_FROM_COMP
FOREIGN KEY (from_compartment_id) REFERENCES SILO_COMPARTMENT(compartment_id);


ALTER TABLE MOVEMENT
ADD CONSTRAINT FK_MOVEMENT_TO_COMP
FOREIGN KEY (to_compartment_id) REFERENCES SILO_COMPARTMENT(compartment_id);


ALTER TABLE SALESORDER
ADD CONSTRAINT FK_SALESORDER_CUSTOMER
FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id);


ALTER TABLE SALESORDER
ADD CONSTRAINT FK_SALESORDER_BRANCH
FOREIGN KEY (branch_id) REFERENCES BRANCH(branch_id);


ALTER TABLE SALESORDERLINE
ADD CONSTRAINT FK_SALESORDERLINE_SALESORDER
FOREIGN KEY (so_id) REFERENCES SALESORDER(so_id);


ALTER TABLE SALESORDERLINE
ADD CONSTRAINT FK_SALESORDERLINE_PRODUCT
FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id);


ALTER TABLE ALLOCATION
ADD CONSTRAINT FK_ALLOCATION_SALESORDERLINE
FOREIGN KEY (sol_id) REFERENCES SALESORDERLINE(sol_id);


ALTER TABLE ALLOCATION
ADD CONSTRAINT FK_ALLOCATION_LOT
FOREIGN KEY (lot_id) REFERENCES LOT(lot_id);

--SONRADAN EKLEME--

ALTER TABLE SILO_COMPARTMENT
ADD product_id INT NULL;

ALTER TABLE SILO_COMPARTMENT
ADD CONSTRAINT FK_SILO_COMPARTMENT_PRODUCT
FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id);

ALTER TABLE SILO_COMPARTMENT
ALTER COLUMN capacity_kg DECIMAL(12,3) NULL;


---------------------------------------------------------------------

CREATE TABLE FARMER (
  farmer_id INT IDENTITY(1,1) PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  tc_no VARCHAR(11) NULL UNIQUE,   
  phone VARCHAR(30) NULL,
  city VARCHAR(50) NULL,
  region VARCHAR(50) NULL,
  cooperative_name VARCHAR(80) NULL
);


CREATE TABLE FARM_PLOT (
  plot_id INT IDENTITY(1,1) PRIMARY KEY,
  farmer_id INT NOT NULL,
  plot_code VARCHAR(50) NOT NULL UNIQUE,
  city VARCHAR(50) NULL,
  district VARCHAR(50) NULL,
  area_decare DECIMAL(10,2) NULL    
);

CREATE TABLE HARVEST (
  harvest_id INT IDENTITY(1,1) PRIMARY KEY,

  farmer_id INT NOT NULL,
  plot_id INT NULL,                      
  product_id INT NOT NULL,

  harvest_year INT NOT NULL,      
  season VARCHAR(20) NULL,          
  harvest_date DATE NULL,

  harvested_kg DECIMAL(12,3) NOT NULL,  
  moisture DECIMAL(5,2) NULL,         
  quality_grade VARCHAR(20) NULL,   
  note VARCHAR(200) NULL
);

ALTER TABLE FARM_PLOT
ADD CONSTRAINT FK_FARM_PLOT_FARMER
FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id);

ALTER TABLE HARVEST
ADD CONSTRAINT FK_HARVEST_FARMER
FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id);

ALTER TABLE HARVEST
ADD CONSTRAINT FK_HARVEST_PLOT
FOREIGN KEY (plot_id) REFERENCES FARM_PLOT(plot_id);

ALTER TABLE HARVEST
ADD CONSTRAINT FK_HARVEST_PRODUCT
FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id);


-- Ayný çiftçi + ürün + yýl + tarla için tekrar kayýt engelle
CREATE UNIQUE INDEX UQ_HARVEST_WITH_PLOT
ON HARVEST (farmer_id, plot_id, product_id, harvest_year)
WHERE plot_id IS NOT NULL;

CREATE UNIQUE INDEX UQ_HARVEST_NO_PLOT
ON HARVEST (farmer_id, product_id, harvest_year)
WHERE plot_id IS NULL;

-----------------------------------------------

ALTER TABLE HARVEST
ADD harvest_area_decare DECIMAL(10,2) NULL;


ALTER TABLE HARVEST
ADD avg_kg_per_decare AS
(
    CASE                                               ----------dekar baþýna düþen kg
        WHEN harvest_area_decare > 0 
        THEN harvested_kg / harvest_area_decare
        ELSE NULL
    END
);
-------------------

ALTER TABLE INTAKE ADD farmer_id INT NOT NULL;
ALTER TABLE INTAKE ADD harvest_year INT NOT NULL;
ALTER TABLE INTAKE ADD CONSTRAINT FK_INTAKE_FARMER FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id);



