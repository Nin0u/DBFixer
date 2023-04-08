DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'null_%typevar') THEN
        CREATE TYPE NULL_%typevar AS (
            version INTEGER,
            value %typevar
        );
    END IF;
END$$;

CREATE OR REPLACE FUNCTION TO_NULL_%typevar(v %typevar) 
RETURNS NULL_%typevar AS $$
DECLARE rep NULL_%typevar;
BEGIN
    rep.value = v;
    rep.version = 0;
    RETURN rep;
END;
$$ LANGUAGE plpgsql;

DROP CAST IF EXISTS (%typevar AS NULL_%typevar);
CREATE CAST(%typevar AS NULL_%typevar) WITH FUNCTION TO_NULL_%typevar(%typevar);

ALTER TABLE  %nomTable 
ALTER COLUMN %nomvar TYPE NULL_%typevar 
USING %nomvar::NULL_%typevar;