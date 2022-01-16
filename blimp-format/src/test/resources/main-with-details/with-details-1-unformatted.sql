--  add foreign key constraint to reference details table id column (case 1)
ALTER TABLE main ADD CONSTRAINT fk_main_2_details FOREIGN KEY (details_id) REFERENCES details (id);
