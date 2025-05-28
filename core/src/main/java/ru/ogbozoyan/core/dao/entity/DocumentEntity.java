package ru.ogbozoyan.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "document")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class DocumentEntity {
    /**/
    @Id
    @Column(name = "uuid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    @Column(name = "s3_key", length = Integer.MAX_VALUE)
    private String s3Key;

    @Column(name = "file_name", length = Integer.MAX_VALUE)
    private String fileName;

    @Column(name = "table_1_url", length = Integer.MAX_VALUE)
    private String table_1_url;

    @Column(name = "table_1_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableBig table_1_Result;

    @Column(name = "table_1_2_url", length = Integer.MAX_VALUE)
    private String table_1_2_url;

    @Column(name = "table_1_2_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_1_2_Result;

    @Column(name = "table_2_1_url", length = Integer.MAX_VALUE)
    private String table_2_1_url;

    @Column(name = "table_2_1_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_2_1_result;

    @Column(name = "table_2_2_url", length = Integer.MAX_VALUE)
    private String table_2_2_url;

    @Column(name = "table_2_2_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_2_2_Result;

    @Column(name = "table_3_1_url", length = Integer.MAX_VALUE)
    private String table_3_1_url;

    @Column(name = "table_3_1_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_3_1_result;

    @Column(name = "table_3_2_url", length = Integer.MAX_VALUE)
    private String table_3_2_url;

    @Column(name = "table_3_2_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_3_2_result;

    @Column(name = "table_4_1_url", length = Integer.MAX_VALUE)
    private String table_4_1_url;

    @Column(name = "table_4_1_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_4_1_result;

    @Column(name = "table_4_2_url", length = Integer.MAX_VALUE)
    private String table_4_2_url;

    @Column(name = "table_4_2_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_4_2_result;

    @Column(name = "table_5_1_url", length = Integer.MAX_VALUE)
    private String table_5_1_url;

    @Column(name = "table_5_1_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_5_1_result;

    @Column(name = "table_5_2_url", length = Integer.MAX_VALUE)
    private String table_5_2_url;

    @Column(name = "table_5_2_result")
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall table_5_2_result;

    @Column(name = "employee_number_url", length = Integer.MAX_VALUE)
    private String employeeNumberUrl;

    @Column(name = "employee_number_result")
    @JdbcTypeCode(SqlTypes.NUMERIC)
    private BigDecimal employeeNumberResult;

    @Column(name = "is_fully_processed")
    @ColumnDefault("false")
    private Boolean isFullyProcessed = Boolean.FALSE;

    @Column(name = "is_split")
    @ColumnDefault("false")
    private Boolean isSplit = Boolean.FALSE;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        DocumentEntity document = (DocumentEntity) o;
        return this.getId() != null && Objects.equals(this.getId(), document.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public String getUrlByTableName(TableNamesEnum tableNameEnum) {
        return switch (tableNameEnum) {
            case TABLE_1 -> this.table_1_url;
            case TABLE_1_2 -> this.table_1_2_url;
            case TABLE_2_1 -> this.table_2_1_url;
            case TABLE_2_2 -> this.table_2_2_url;
            case TABLE_3_1 -> this.table_3_1_url;
            case TABLE_3_2 -> this.table_3_2_url;
            case TABLE_4_1 -> this.table_4_1_url;
            case TABLE_4_2 -> this.table_4_2_url;
            case TABLE_5_1 -> this.table_5_1_url;
            case TABLE_5_2 -> this.table_5_2_url;
            case LAST_NUMBER_TABLE -> this.employeeNumberUrl;
        };
    }
}