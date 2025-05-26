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
    @JdbcTypeCode(SqlTypes.JSON)
    private TableSmall employeeNumberResult;

    @Column(name = "is_fully_processed")
    @ColumnDefault("false")
    private Boolean isFullyProcessed = Boolean.FALSE;

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

    public record TableBig(
        String tableName,

        Integer column1,
        Integer result1,

        Integer column2,
        Integer result2,

        Integer column3,
        Integer result3,

        Integer column4,
        Integer result4,

        Integer column5,
        Integer result5,

        Integer column6,
        Integer result6,

        Integer column7,
        Integer result7,

        Integer column8,
        Integer result8,

        Integer column9,
        Integer result9,

        Integer column10,
        Integer result10,

        Integer column11,
        Integer result11,

        Integer column12,
        Integer result12,

        Integer column13,
        Integer result13,

        Integer column14,
        Integer result14,

        Integer column15,
        Integer result15,

        Integer column16,
        Integer result16,

        Integer column17,
        Integer result17,

        Integer column18,
        Integer result18,

        Integer column19,
        Integer result19,

        Integer column20,
        Integer result20
    ) {
    }

    public record TableSmall(
        String tableName,

        Integer column1,
        Integer result1,

        Integer column2,
        Integer result2,

        Integer column3,
        Integer result3,

        Integer column4,
        Integer result4,

        Integer column5,
        Integer result5,

        Integer column6,
        Integer result6,

        Integer column7,
        Integer result7
    ) {
    }

}