package com.theshowsoftware.InternalTestPage.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.theshowsoftware.InternalTestPage.repository"
)
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(dbUsername);
        hikariConfig.setPassword(dbPassword);
        hikariConfig.setDriverClassName(dbDriverClassName);
        hikariConfig.setPoolName("PostgreSQLPool");
        hikariConfig.setMinimumIdle(5);  // 최소 유휴 커넥션 수
        hikariConfig.setMaximumPoolSize(200);  // 최대 커넥션 수
        hikariConfig.setIdleTimeout(10000);  // 유휴 커넥션 유지 시간(ms)
        hikariConfig.setMaxLifetime(420000);  // 커넥션 최대 생명 시간(ms)
        hikariConfig.setConnectionTimeout(5000);  // 커넥션 대기 시간(ms)
        hikariConfig.setLeakDetectionThreshold(2000);  // 커넥션 누수 감지 시간(ms)
        hikariConfig.setValidationTimeout(5000);  // 커넥션 유효성 검사 시간(ms)

        return new HikariDataSource(hikariConfig);
    }
}