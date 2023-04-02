CREATE TYPE NULL_%nomvar AS (
    version INTEGER,
    value %typevar
);

CREATE OR REPLACE FUNCTION TO_NULL_%nomvar(v %typevar) 
RETURNS NULL_%nomvar AS $$
DECLARE rep NULL_%nomvar;
BEGIN
    rep.value = v;
    rep.version = %numnull;
    RETURN rep;
END;
$$ LANGUAGE plpgsql;

DROP CAST IF EXISTS (%typevar AS NULL_%nomvar);
CREATE CAST(%typevar AS NULL_%nomvar) WITH FUNCTION TO_NULL_%nomvar(%typevar);

ALTER TABLE %nomTable
ALTER COLUMN %nomvar TYPE NULL_%nomvar
USING %nomvar::NULL_%nomvar;