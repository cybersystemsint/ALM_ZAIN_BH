package com.zain.bh.alm.acceptance.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class SmsService {

    private static final Logger LOGGER = LogManager.getLogger(SmsService.class);
    private static final String SMSC_ACCOUNT = "tkinternal";
    private static final String SMSC_PASSWORD = "1234";
    private static final String SMSC_SHORT_CODE = "254189948";
    private static final String SMSC_URL = "http://192.168.27.47:13013/cgi-bin/sendsms";

    public static void sendSms(String msisdn, String message, String requestID) {
        try {
            String url = SMSC_URL + "?username=" + SMSC_ACCOUNT + "&password=" + SMSC_PASSWORD + "&to=" + msisdn + "&from=" + SMSC_SHORT_CODE + "&text=" + URLEncoder.encode(message);

            URL obj = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();

            FileLogUtil.logToFile(" | " + requestID + " | " + msisdn + " | " + "Sent SMS ", "INFO");
            FileLogUtil.logToFile(" | " + requestID + " | " + msisdn + " | " + "Sent SMS response code " + responseCode + " message " + message, "INFO");

            LOGGER.info("SMS sent successfully. Response code: {}", responseCode);
        } catch (Exception e) {
            LOGGER.error("Error sending SMS to {}", msisdn, e);
        }
    }
}
