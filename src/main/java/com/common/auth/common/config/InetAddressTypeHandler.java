package com.common.auth.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.sql.SQLException;

@MappedTypes(InetAddress.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class InetAddressTypeHandler extends BaseTypeHandler<InetAddress> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, InetAddress parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.getHostAddress(), java.sql.Types.OTHER);
    }

    @Override
    public InetAddress getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String ipString = rs.getString(columnName);
        return parseInetAddress(ipString);
    }

    @Override
    public InetAddress getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String ipString = rs.getString(columnIndex);
        return parseInetAddress(ipString);
    }

    @Override
    public InetAddress getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String ipString = cs.getString(columnIndex);
        return parseInetAddress(ipString);
    }

    private InetAddress parseInetAddress(String ipString) throws SQLException {
        if (ipString == null || ipString.trim().isEmpty()) {
            return null;
        }
        try {
            return InetAddress.getByName(ipString);
        } catch (UnknownHostException e) {
            throw new SQLException("Invalid IP address: " + ipString, e);
        }
    }
}