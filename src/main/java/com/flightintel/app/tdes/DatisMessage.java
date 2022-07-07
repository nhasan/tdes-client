package com.flightintel.app.tdes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DatisMessage {
    private static final Logger logger = LoggerFactory.getLogger(DatisMessage.class);

    private final String icaoLocation;
    private final Timestamp issuedTimestamp;
    private final String atisType;
    private final String atisCode;
    private final String atisHeader;
    private final String atisBody;
    private final String xmlMessage;

    public DatisMessage(final String xmlMessage) throws DAtisMessageParseException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(xmlMessage.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(stream);
            Element datisData = doc.getDocumentElement();
            this.icaoLocation = datisData.getElementsByTagName("airportID").item(0).getTextContent();
            this.issuedTimestamp = parseAtisTime(datisData.getElementsByTagName("time").item(0).getTextContent());
            this.atisType = datisData.getElementsByTagName("editType").item(0).getTextContent();
            this.atisCode = datisData.getElementsByTagName("atisCode").item(0).getTextContent();
            this.atisHeader = datisData.getElementsByTagName("dataHeader").item(0).getTextContent();
            this.atisBody = datisData.getElementsByTagName("dataBody").item(0).getTextContent();
            this.xmlMessage = xmlMessage;
        } catch (Exception e) {
            throw new DAtisMessageParseException("Failed to create DAtis message due to: " + e.getMessage(), e);
        }
    }

    private Timestamp parseAtisTime(String value)
    {
        int hour = Integer.parseInt(value.substring(0, 2));
        int minute = Integer.parseInt(value.substring(2));
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int time1 = Integer.parseInt(value);
        int time2 = calendar.get(Calendar.HOUR_OF_DAY)*100 + calendar.get(Calendar.MINUTE);
        if (time1 > time2) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        return new Timestamp(calendar.getTimeInMillis());
    }

    public String getIcaoLocation() {
        return icaoLocation;
    }

    public Timestamp getIssuedTimestamp() {
        return issuedTimestamp;
    }

    public String getAtisType() {
        return atisType;
    }

    public String getAtisCode() {
        return atisCode;
    }

    public String getAtisHeader() {
        return atisHeader;
    }

    public String getAtisBody() {
        return atisBody;
    }

    public String getXmlMessage() {
        return xmlMessage;
    }

    public static class DAtisMessageParseException extends Exception
    {
        public DAtisMessageParseException(String message, Exception e)
        {
            super(message, e);
        }
    }
}
