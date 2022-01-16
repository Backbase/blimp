--  add column and foreign key constraint to reference details table id column (case 2)
ALTER TABLE main 
    ADD details_id VARCHAR(50) NULL;

ALTER TABLE main 
    ADD CONSTRAINT fk_main_2_details 
    FOREIGN KEY (details_id) 
    REFERENCES details (id);
