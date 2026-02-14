package com.zain.bh.alm.acceptance.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = "com.zain.bh.alm.acceptance.repository",
		entityManagerFactoryRef = "mysqlEntityManagerFactory",
		transactionManagerRef = "mysqlTransactionManager")
public class DataSourceConfig {

	@Primary
	@Bean(name = {"mysqlDataSource", "dataSource"})
	public DataSource mysqlDataSource() {
		return DataSourceBuilder.create()
				.url("jdbc:mysql://217.160.157.126:3306/ALM_ZAIN_BH")
				.username("root")
				.password("ALMDev@2025!")
				.driverClassName("com.mysql.cj.jdbc.Driver")
				.build();
	}

	@Primary
	@Bean(name = {"mysqlEntityManagerFactory", "entityManagerFactory"})
	public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
			EntityManagerFactoryBuilder builder,
			@Qualifier("mysqlDataSource") DataSource dataSource) {
		return builder
				.dataSource(dataSource)
				.packages("com.zain.bh.alm.acceptance.entity")
				.persistenceUnit("mysql")
				.build();
	}

	@Primary
	@Bean(name = "mysqlTransactionManager")
	public PlatformTransactionManager mysqlTransactionManager(
			@Qualifier("mysqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

}
