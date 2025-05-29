package ru.ogbozoyan.core.dao.entity;

public enum TableNamesEnum {
    TABLE_1("table_1.png"),
    TABLE_1_2("table_1_2.png"),
    TABLE_2_1("table_2_1.png"),
    TABLE_2_2("table_2_2.png"),
    TABLE_3_1("table_3_1.png"),
    TABLE_3_2("table_3_2.png"),
    TABLE_4_1("table_4_1.png"),
    TABLE_4_2("table_4_2.png"),
    TABLE_5_1("table_5_1.png"),
    TABLE_5_2("table_5_2.png"),
    LAST_NUMBER_TABLE("last_number.png"),
    ;


    TableNamesEnum(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("filename cannot be null");
        }
    }

    public String getName() {
        return name();
    }

}
