package com.flightintel.app.tdes.jms;

import com.flightintel.app.tdes.DatisMessage;
import com.flightintel.app.tdes.datisdb.DatisDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.sql.SQLException;

public class TdesJmsMessageWorker implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(TdesJmsMessageWorker.class);

    private final DatisDb datisDb;

    public TdesJmsMessageWorker(DatisDb datisDb) {
        this.datisDb = datisDb;
    }

    @Override
    public void onMessage(Message jmsMessage) {
        try {
            DatisMessage datisMessage = parseTdesJmsMessage(jmsMessage);
            logger.debug("Received: "+datisMessage.getIcaoLocation()+" "+datisMessage.getAtisType()
                    +" "+datisMessage.getAtisCode() );
            try {
                datisDb.removeOldDatis(datisMessage);
                datisDb.putDatis(datisMessage);
            }
            catch (SQLException e) {
                logger.warn("Failed to insert datis into database", e);
            }
        } catch (Exception e) {
            logger.error("Failed to processed JMS Text Message due to: " + e.getMessage());
        }
    }

    private DatisMessage parseTdesJmsMessage(Message message) throws Exception {
        String messageBody = "";
        if (message instanceof BytesMessage) {
            BytesMessage byteMessage = (BytesMessage) message;
            byte[] messageBytes = new byte[(int) byteMessage.getBodyLength()];
            byteMessage.readBytes(messageBytes);
            messageBody = new String(messageBytes);
        } else if (message instanceof TextMessage) {
            messageBody = ((TextMessage) message).getText();
        }

        return new DatisMessage(messageBody);
    }
}
