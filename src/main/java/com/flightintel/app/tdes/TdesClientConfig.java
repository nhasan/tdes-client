package com.flightintel.app.tdes;

import com.flightintel.app.tdes.datisdb.DatisDbConfig;
import com.typesafe.config.Config;
import us.dot.faa.swim.jms.JmsClientConfig;

public class TdesClientConfig {
    protected DatisDbConfig datisDbConfig = new DatisDbConfig();
    protected JmsClientConfig jmsClientConfig = new JmsClientConfig();

    protected String jmsConnectionFactoryName = "";
    protected String jmsDestination = "";

    public TdesClientConfig(Config typeSafeConfig) {
        this.jmsClientConfig.setInitialContextFactory(typeSafeConfig.getString("jms.initialContextFactory"));
        this.jmsClientConfig.setProviderUrl(typeSafeConfig.getString("jms.providerUrl"));
        this.jmsClientConfig.setUsername(typeSafeConfig.getString("jms.username"));
        this.jmsClientConfig.setPassword(typeSafeConfig.getString("jms.password"));
        this.jmsClientConfig.setSolaceMessageVpn(typeSafeConfig.getString("jms.solace.messageVpn"));
        this.jmsClientConfig.setSolaceSslTrustStore(typeSafeConfig.getString("jms.solace.sslTrustStore"));
        this.jmsClientConfig.setSolaceJndiConnectionRetries(typeSafeConfig.getInt("jms.solace.jndiConnectionRetries"));
        this.jmsConnectionFactoryName = typeSafeConfig.getString("jms.connectionFactory");
        this.jmsDestination = typeSafeConfig.getString("jms.destination");

        this.datisDbConfig.setDriver(typeSafeConfig.getString("datisDb.driver"));
        this.datisDbConfig.setConnectionUrl(typeSafeConfig.getString("datisDb.connectionUrl"));
        this.datisDbConfig.setUsername(typeSafeConfig.getString("datisDb.username"));
        this.datisDbConfig.setPassword(typeSafeConfig.getString("datisDb.password"));
        this.datisDbConfig.setSchema(typeSafeConfig.getString("datisDb.schema"));
        this.datisDbConfig.setTable(typeSafeConfig.getString("datisDb.table"));
    }

    public String getJmsConnectionFactoryName() {
        return this.jmsConnectionFactoryName;
    }

    public String getJmsDestination() {
        return this.jmsDestination;
    }

}
