package com.telkom.almBHZain.helper;


import com.telkom.almBHZain.controller.APIController;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class helper {
    public static void logBatchFile(String log, boolean includeDate,String batchfilename){
        try{
            String baseloc = "/home/app/logs/ALM/BatchFiles/"+batchfilename;
            FileWriter f = new FileWriter(new File(baseloc),true);
            if(includeDate)
                f.write(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now() ) + System.lineSeparator());
            f.write(log + System.lineSeparator());
            f.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


    public static  void sendSms(String msisdn,String message, String requestID){
        try{
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String smscAccount="tkinternal";
            String smscPassword="1234";
            String smscShortCode="254189948";
            String smscUrl="http://192.168.27.47:13013/cgi-bin/sendsms";
//            message =message.replaceAll(" ", "+");
//            String sms=message.replaceAll(" ", "+");
//            sms = URLEncoder.encode(message);
            String url = smscUrl+"?username="+smscAccount+"&password="+smscPassword+"&to=" + msisdn + "&from="+smscShortCode+"&text=" +  URLEncoder.encode(message);

            URL obj = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();

//            helper.logToFile("==================================================",false);
//            helper.logToFile("timestamp  "+ myFormatObj.format(now),false);
//            helper.logToFile("Msisdn "+ msisdn,false);
//            helper.logToFile("Request ID "+ requestID,false);
//            helper.logToFile("Sent SMS "+message,true);

            helper.logToFile(" | " + requestID + " | " + msisdn + " | " + "Sent SMS ", "INFO");
            helper.logToFile(" | " + requestID + " | " + msisdn + " | "+ "Sent SMS response code " + responseCode+" message "+message, "INFO");

            System.out.println(responseCode);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void renameFile(){
        Date d = new Date();
        String fname = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now().minusDays(1));
        Path source = Paths.get("/home/app/logs/ALM/Subrack/Subrack.log");
        try{
            // rename a file in the same directory
            Files.move(source, source.resolveSibling("Subrack.log-"+ fname));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getKenyaDateTimeString() {
        String formattedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        return formattedDateTime;
    }

    public static void logToFile(String log, String status){
        try{
            String baseloc = "/home/app/logs/ALM/Subrack/Subrack.log";
            try {
                Path source = Paths.get("/home/app/logs/ALM/Subrack/Subrack.log");
                FileTime creationTime = (FileTime) Files.getAttribute(source, "creationTime");
                LocalDateTime convertedFileTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = sdf.parse(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(convertedFileTime));
                Date date2 = sdf.parse(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()));

                int result = date1.compareTo(date2);
                if(result!=0){
                    renameFile();
                }

            } catch (IOException ex) {
                // handle exception
            }

            FileWriter f = new FileWriter(new File(baseloc),true);
            f.write(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now() ) +" : "+ status +" : "+ log + System.lineSeparator());
            f.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}

