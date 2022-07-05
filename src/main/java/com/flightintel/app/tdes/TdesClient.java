package com.flightintel.app.tdes;

import com.flightintel.app.tdes.datisdb.DatisDb;
import com.flightintel.app.tdes.jms.TdesJmsMessageWorker;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.dot.faa.swim.jms.JmsClient;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public class TdesClient implements ExceptionListener {
    private static final Logger logger = LoggerFactory.getLogger(TdesClient.class);
    private final static CountDownLatch latch = new CountDownLatch(1);

    private final TdesClientConfig config;
    private final JmsClient jmsClient;
    private final TdesJmsMessageWorker tdesJmsMessageWorker;

    private final DatisDb datisDb;

    public TdesClient(TdesClientConfig config) throws Exception {
        this.config = config;

        jmsClient = new JmsClient(config.jmsClientConfig);
        datisDb = new DatisDb(config.datisDbConfig);

        tdesJmsMessageWorker = new TdesJmsMessageWorker(datisDb);
    }

    private void connectJmsClient() {

        logger.info("Starting JMS Consumer");

        while (true) {
            try {
                jmsClient.connect(config.getJmsConnectionFactoryName(), this);
                jmsClient.createConsumer(config.getJmsDestination()).setMessageListener(tdesJmsMessageWorker);
                break;
            } catch (final Exception e) {
                logger.error("JmsClient failed to start due to: " + e.getMessage(), e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    logger.warn("Thread interrupted");
                }
            }
        }
        logger.info("JMS Consumer Started");
    }

    public void start() throws SQLException {
        logger.info("Starting TdesClient");
        if (datisDb.datisTableExists()) {
            datisDb.dropDatisTable();
        }
        datisDb.createDatisTable();

        connectJmsClient();
    }

    public void stop() {
        try {
            logger.info("Destroying JmsClient");
            jmsClient.close();
            logger.info("Closing database connections");
            datisDb.close();
        } catch (final Exception e) {
            logger.error("Unable to destroy JmsClient due to: " + e.getMessage(), e);
        }
    }

    @Override
    public void onException(JMSException e) {
        logger.error("JmsClient Failure due to : " + e.getMessage() + ". Restarting JmsClient", e);
        try {
            jmsClient.reInitialize();
        } catch (final Exception e1) {
            logger.error(
                    "Failed to JmsClient JmsClient due to : " + e1.getMessage() + ". Continuing with JmsClient Restart",
                    e1);
        }

        connectJmsClient();
    }

    private static class ShutdownHook extends Thread {
        final TdesClient tdesClient;

        ShutdownHook(TdesClient tdesClient) {
            this.tdesClient = tdesClient;
        }

        @Override
        public void run() {
            logger.info("Shutting Down...");

            tdesClient.stop();
        }
    }

    public static void main(final String[] args) throws Exception {
        logger.info("Loading TdesClient Config and Initializing");

        Config typeSafeConfig;
        // load FnsClient Config
        if (Files.exists(Paths.get("tdesClient.conf"))) {
            typeSafeConfig = ConfigFactory.parseFile(new File("tdesClient.conf"));
        } else {
            typeSafeConfig = ConfigFactory.load();
        }

        TdesClient tdesClient = new TdesClient(new TdesClientConfig(typeSafeConfig));
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(tdesClient));
        tdesClient.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.info("Main Thread Interrupted, Exiting...");
        }
    }
}
