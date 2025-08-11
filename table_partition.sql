CREATE TABLE my_table_new (
    id        NUMBER,
    active    NUMBER(1),
    data      VARCHAR2(100)
)
PARTITION BY LIST (active) (
    PARTITION p_active_0 VALUES (0),
    PARTITION p_active_1 VALUES (1)
);

ALTER TABLE my_table_new ENABLE ROW MOVEMENT;

-- Step 3: Use DBMS_REDEFINITION to sync & switch
BEGIN
    DBMS_REDEFINITION.START_REDEF_TABLE(
        uname       => 'MY_SCHEMA',
        orig_table  => 'MY_TABLE',
        int_table   => 'MY_TABLE_PART'
    );

    DBMS_REDEFINITION.FINISH_REDEF_TABLE(
        uname       => 'MY_SCHEMA',
        orig_table  => 'MY_TABLE',
        int_table   => 'MY_TABLE_PART'
    );
END;

INSERT /*+ APPEND */ INTO my_table_new
SELECT * FROM my_table;


COMMIT;
