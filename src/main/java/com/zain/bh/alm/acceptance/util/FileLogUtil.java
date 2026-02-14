package com.zain.bh.alm.acceptance.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class FileLogUtil {

    private static final Logger LOGGER = LogManager.getLogger(FileLogUtil.class);
    private static final String LOG_BASE_PATH = "/home/app/logs/ALM/Subrack/Subrack.log";
    private static final String BATCH_BASE_PATH = "/home/app/logs/ALM/BatchFiles/";

    public static void logBatchFile(String log, boolean includeDate, String batchfilename) {
        try {
            String baseloc = BATCH_BASE_PATH + batchfilename;
            FileWriter f = new FileWriter(new File(baseloc), true);
            if (includeDate)
                f.write(DateTimeUtil.getFormattedTime() + System.lineSeparator());
            f.write(log + System.lineSeparator());
            f.close();
        } catch (Exception e) {
            LOGGER.error("Error writing to batch file: {}", batchfilename, e);
        }
    }

    public static void logToFile(String log, String status) {
        try {
            checkAndRotateLogFile();
            FileWriter f = new FileWriter(new File(LOG_BASE_PATH), true);
            f.write(DateTimeUtil.getFormattedDateTime() + " : " + status + " : " + log + System.lineSeparator());
            f.close();
        } catch (Exception e) {
            LOGGER.error("Error writing to log file", e);
        }
    }

    private static void checkAndRotateLogFile() {
        try {
            Path source = Paths.get(LOG_BASE_PATH);
            FileTime creationTime = (FileTime) Files.getAttribute(source, "creationTime");
            LocalDateTime convertedFileTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = sdf.parse(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(convertedFileTime));
            Date date2 = sdf.parse(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()));

            int result = date1.compareTo(date2);
            if (result != 0) {
                rotateLogFile();
            }
        } catch (Exception ex) {
            LOGGER.warn("Error checking log file rotation", ex);
        }
    }

    private static void rotateLogFile() {
        String fname = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now().minusDays(1));
        Path source = Paths.get(LOG_BASE_PATH);
        try {
            Files.move(source, source.resolveSibling("Subrack.log-" + fname));
        } catch (IOException e) {
            LOGGER.error("Error rotating log file", e);
        }
    }
}
