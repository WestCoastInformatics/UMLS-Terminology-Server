-- Disable foreign key constraints
SET FOREIGN_KEY_CHECKS = 0;
-- Find tables to drop
SET GROUP_CONCAT_MAX_LEN=32768;
SET @tables = NULL;
SELECT GROUP_CONCAT(table_name) INTO @tables
  FROM information_schema.tables
  WHERE table_schema = (SELECT DATABASE());
SELECT IFNULL(@tables,'dummy') INTO @tables;
-- Drop tables
SET @tables = CONCAT('DROP TABLE IF EXISTS ', @tables);
PREPARE stmt FROM @tables;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- Reenable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

-- See import.sql, any view or table created should be dropped here
drop view if exists classes_m4;
drop view if exists auis_m4;
drop view if exists ruis_m4;
drop view if exists ambig_concepts;
drop table if exists deep_atom_relationships;
drop table if exists deep_concept_relationships;