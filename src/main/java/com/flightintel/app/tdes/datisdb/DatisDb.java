package com.flightintel.app.tdes.datisdb;

import com.flightintel.app.tdes.DatisMessage;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatisDb {
    private final static Logger logger = LoggerFactory.getLogger(DatisDb.class);
    private final DatisDbConfig config;

    private final BasicDataSource notamDbDataSource = new BasicDataSource();

    public DatisDb(DatisDbConfig config) throws Exception {
        this.config = config;
        if (!config.getDriver().equals("org.postgresql.Driver")) {
            throw new Exception("DB Driver: " + config.getDriver()
                    + " currently not supported. Only postgresql is supported.");
        }

        logger.info("Initializing database connection pool");
        notamDbDataSource.setDriverClassName(config.getDriver());
        notamDbDataSource.setUrl(config.getConnectionUrl());
        notamDbDataSource.setUsername(config.getUsername());
        notamDbDataSource.setPassword(config.getPassword());
        notamDbDataSource.setMinIdle(0);
        notamDbDataSource.setMaxIdle(10);
        notamDbDataSource.setMaxOpenPreparedStatements(100);
        notamDbDataSource.start();
    }

    public boolean datisTableExists() throws SQLException {
        Connection conn = null;
        ResultSet rset = null;
        try {
            conn = getDBConnection();
            rset = conn.getMetaData().getTables(null, this.config.schema, this.config.table, null);
            if (rset.next()) {
                return true;
            }
        } finally {
            DbUtils.closeQuietly(rset);
            DbUtils.closeQuietly(conn);
        }
        return false;
    }

    public void dropDatisTable() throws SQLException {
        Connection conn = getDBConnection();
        try {
            if (datisTableExists()) {
                logger.info("Dropping datis table");
                final String dropQuery = "DROP TABLE " + this.config.table;
                conn.prepareStatement(dropQuery).execute();
            }
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    public void createDatisTable() throws SQLException {
        Connection conn = getDBConnection();
        try {
            logger.info("Creating datis table");
            String createQuery = "CREATE TABLE " + this.config.table + " ( "
                    + "icaoLocation varchar(12), issuedTimeStamp timestamptz, atisType varchar(1), "
                    + "atisCode varchar(1), atisHeader varchar(64), atisBody varchar(2048), "
                    + "storedTimeStamp timestamptz, xmlMessage xml, primary key (icaoLocation, atisType))";
            conn.prepareStatement(createQuery).execute();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    public void putDatis(final DatisMessage datisMessage) throws SQLException {
        Connection conn = null;
        try {
            conn = getDBConnection();
            String putDatisSql = "INSERT INTO " + this.config.table + " VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON CONFLICT (icaoLocation, atisType) DO UPDATE SET issuedTimeStamp=EXCLUDED.issuedTimeStamp, "
                    + "atisCode=EXCLUDED.atisCode, atisHeader=EXCLUDED.atisHeader, atisBody=EXCLUDED.atisBody, "
                    + "storedTimeStamp=NOW(), xmlMessage=EXCLUDED.xmlMessage";

            PreparedStatement putDatisPreparedStatement = conn.prepareStatement(putDatisSql);
            putDatisPreparedStatement.setString(1, datisMessage.getIcaoLocation());
            putDatisPreparedStatement.setTimestamp(2, datisMessage.getIssuedTimestamp());
            putDatisPreparedStatement.setString(3, datisMessage.getAtisType());
            putDatisPreparedStatement.setString(4, datisMessage.getAtisCode());
            putDatisPreparedStatement.setString(5, datisMessage.getAtisHeader());
            putDatisPreparedStatement.setString(6, datisMessage.getAtisBody());
            putDatisPreparedStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            SQLXML sqlXml = putDatisPreparedStatement.getConnection().createSQLXML();
            sqlXml.setString(datisMessage.getXmlMessage());
            putDatisPreparedStatement.setSQLXML(8, sqlXml);

            putDatisPreparedStatement.executeUpdate();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    public void removeOldDatis(final DatisMessage datisMessage) throws SQLException {
        Connection conn = getDBConnection();
        PreparedStatement removeDatisPreparedStatement = null;
        try {
            if (datisMessage.getAtisType().equals("C")) {
                removeDatisPreparedStatement = conn.prepareStatement(
                        "DELETE FROM datis WHERE icaoLocation=? and atisType <> 'C'");
            } else {
                removeDatisPreparedStatement = conn.prepareStatement(
                        "DELETE FROM datis WHERE icaoLocation=? and atisType = 'C'");
            }

            removeDatisPreparedStatement.setString(1, datisMessage.getIcaoLocation());

            removeDatisPreparedStatement.executeUpdate();
        } finally {
            DbUtils.closeQuietly(removeDatisPreparedStatement);
            DbUtils.closeQuietly(conn);
        }
    }

    public Connection getDBConnection() throws SQLException {
        return notamDbDataSource.getConnection();
    }

    public void close() throws SQLException {
        notamDbDataSource.close();
    }
}
