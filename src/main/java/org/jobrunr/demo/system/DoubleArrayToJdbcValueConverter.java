package org.jobrunr.demo.system;

import oracle.jdbc.OracleType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import org.springframework.jdbc.core.SqlTypeValue;

import java.sql.SQLException;

public class DoubleArrayToJdbcValueConverter implements Converter<double[], JdbcValue> {

    @Override
    public JdbcValue convert(double[] source) {
        SqlTypeValue oracleArrayBinder = new SqlTypeValue() {
            @Override
            public void setTypeValue(java.sql.PreparedStatement ps, int paramIndex, int sqlType, String typeName) throws SQLException {
                ps.setObject(paramIndex, source, OracleType.VECTOR_FLOAT32);
            }
        };
        return JdbcValue.of(oracleArrayBinder, OracleType.VECTOR_FLOAT32);
    }

}
