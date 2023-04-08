DROP TABLE IF EXISTS Table1;
DROP TABLE IF EXISTS Table2;

CREATE TABLE Table1 (
    a VARCHAR,
    b VARCHAR
);

CREATE TABLE Table2 (
    a VARCHAR,
    b VARCHAR,
    c VARCHAR
);

INSERT INTO Table1 VALUES 
('1', '1'),
('2', '2');

INSERT INTO Table2 VALUES
('1', '2', '3');