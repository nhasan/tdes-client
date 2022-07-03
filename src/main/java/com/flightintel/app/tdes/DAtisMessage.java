package com.flightintel.app.tdes;

import com.jcraft.jzlib.ZStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DAtisMessage {
    private static final Logger logger = LoggerFactory.getLogger(DAtisMessage.class);

    private final String icaoLocation;
    private final Timestamp issuedTimestamp;
    private final String atisType;
    private final String atisCode;
    private final String atisBody;

    public DAtisMessage(final String xmlMessage) throws DAtisMessageParseException {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(xmlMessage.getBytes("UTF-8"));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            DateFormat timestampFormater = new SimpleDateFormat("ddHHmm");
            timestampFormater.setTimeZone(TimeZone.getTimeZone("UTC"));

            Document doc = builder.parse(stream);
            Element datisData = doc.getDocumentElement();
            icaoLocation = datisData.getElementsByTagName("airportID").item(0).getTextContent();
            String s = datisData.getElementsByTagName("DATISTime").item(0).getTextContent();
            issuedTimestamp = new Timestamp(timestampFormater.parse(s).getTime());
            atisType = datisData.getElementsByTagName("editType").item(0).getTextContent();
            atisCode = datisData.getElementsByTagName("atisCode").item(0).getTextContent();
            atisBody = datisData.getElementsByTagName("dataBody").item(0).getTextContent();

            stream.close();
        } catch (Exception e) {
            throw new DAtisMessageParseException("Failed to create DAtis message due to: "+e.getMessage(), e);
        }
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

    public String getAtisBody() {
        return atisBody;
    }

    public static class DAtisMessageParseException extends Exception
    {
        public DAtisMessageParseException(String message, Exception e)
        {
            super(message, e);
        }
    }
}
