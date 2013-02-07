UPDATE m_product_loan SET name = CONCAT(id,'-',name); 

-- concating unique value to make sure column does not contain any duplicate value

ALTER TABLE m_product_loan
ADD CONSTRAINT unq_name UNIQUE (name);