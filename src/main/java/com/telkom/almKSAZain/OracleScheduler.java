//package com.telkom.almKSAZain;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import javax.sql.DataSource;
//import org.springframework.beans.factory.annotation.Qualifier;
//
//@Component
//public class OracleScheduler {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    public OracleScheduler(@Qualifier("oracleDataSource") DataSource oracleDataSource) {
//        this.jdbcTemplate = new JdbcTemplate(oracleDataSource);
//    }
//
//    @Scheduled(fixedRate = 60000) // Runs every 60 seconds, adjust as needed
//    public void fetchTopFiveItems() {
//        String query = "SELECT * FROM APPS.ZAIN_KSA_ITEM_RELATIONS FETCH FIRST 5 ROWS ONLY";
//
//        jdbcTemplate.query(query, (rs) -> {
//            System.out.println("Item: " + rs.getString("ITEM_CODE"));
//        });
//    }
//}
