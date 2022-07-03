package com.flightintel.app.tdes.datisdb;

public class DatisDbConfig {
    protected String driver = "org.postgresql.Driver";
    protected String connectionUrl = "jdbc:postgresql://postgres:5432/postgres";
    protected String username = "";
    protected String password = "";
    protected String schema = "public";
    protected String table = "datis";

    public String getDriver() {
        return this.driver;
    }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getTable() {
        return this.table;
    }

    public DatisDbConfig setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public DatisDbConfig setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public DatisDbConfig setUsername(String username) {
        this.username = username;
        return this;
    }

    public DatisDbConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public DatisDbConfig setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public DatisDbConfig setTable(String table) {
        this.table = table;
        return this;
    }

}
