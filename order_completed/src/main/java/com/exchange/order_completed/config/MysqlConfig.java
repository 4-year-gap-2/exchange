package com.exchange.order_completed.config;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.exchange.order_completed.infrastructure.mysql.repository",
        entityManagerFactoryRef = "mysqlEntityManagerFactory",
        transactionManagerRef = "mysqlTransactionManager"
)
public class MysqlConfig {


    @Value("${spring.mysql.datasource.driver-class-name}")
    private String driver;
    @Value("${spring.mysql.datasource.password}")
    private String password;
    @Value("${spring.mysql.datasource.url}")
    private String url;
    @Value("${spring.mysql.datasource.username}")
    private String username;

    @Bean
    public DataSource mysqlDataSource() {

        return DataSourceBuilder.create()
                .url(url)
                .password(password)
                .driverClassName(driver)
                .username(username)
                .build();
    }



    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("mysqlDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");

        return builder
                .dataSource(dataSource)
                .packages("com.exchange.order_completed.domain.entiry")
                .properties(properties)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(@Qualifier("mysqlEntityManagerFactory")EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
        return transactionManager;
    }


}