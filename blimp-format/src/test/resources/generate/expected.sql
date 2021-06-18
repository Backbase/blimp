CREATE TABLE product (
   id BIGINT AUTO_INCREMENT NOT NULL,
    create_date datetime NOT NULL COMMENT 'The date when the product was created',
    name VARCHAR(255) NOT NULL COMMENT 'The name of the product',
    weight SMALLINT NULL COMMENT 'The weight of the product in kgs',
    CONSTRAINT pk_product PRIMARY KEY (id)
) COMMENT='Table to store the Products from our store';
