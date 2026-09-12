USE hxcoe003;

DELIMITER //
DROP PROCEDURE IF EXISTS normalize_nonprefixed_tables//
CREATE PROCEDURE normalize_nonprefixed_tables()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'customers'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'scrm_customer'
    ) THEN
      CREATE TABLE scrm_customer LIKE customers;
      INSERT INTO scrm_customer SELECT * FROM customers;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'opportunities'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'opportunity_opportunity'
    ) THEN
      CREATE TABLE opportunity_opportunity LIKE opportunities;
      INSERT INTO opportunity_opportunity SELECT * FROM opportunities;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'logistics_order'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'les_logistics_order'
    ) THEN
      CREATE TABLE les_logistics_order LIKE logistics_order;
      INSERT INTO les_logistics_order SELECT * FROM logistics_order;
    END IF;
  END IF;
END//
DELIMITER ;

CALL normalize_nonprefixed_tables();
DROP PROCEDURE normalize_nonprefixed_tables;
