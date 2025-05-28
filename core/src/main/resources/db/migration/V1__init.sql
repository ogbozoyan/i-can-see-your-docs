CREATE TABLE IF NOT EXISTS document (
    uuid UUID PRIMARY KEY,
    s3_key VARCHAR,
    file_name VARCHAR,

    table_1_url VARCHAR,
    table_1_result JSONB,

    table_1_2_url VARCHAR,
    table_1_2_result JSONB,

    table_2_1_url VARCHAR,
    table_2_1_result JSONB,

    table_2_2_url VARCHAR,
    table_2_2_result JSONB,

    table_3_1_url VARCHAR,
    table_3_1_result JSONB,

    table_3_2_url VARCHAR,
    table_3_2_result JSONB,

    table_4_1_url VARCHAR,
    table_4_1_result JSONB,

    table_4_2_url VARCHAR,
    table_4_2_result JSONB,

    table_5_1_url VARCHAR,
    table_5_1_result JSONB,

    table_5_2_url VARCHAR,
    table_5_2_result JSONB,

    employee_number_url VARCHAR,
    employee_number_result JSONB,

    is_fully_processed BOOLEAN default false,

    is_split BOOLEAN default false
);
