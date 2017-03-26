package basics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class TransactionalConfiguration {

 // <1>
 @Bean
 DataSource dataSource() {
  SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
  dataSource
   .setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
  dataSource.setDriverClass(org.h2.Driver.class);
  dataSource.setUsername("sa");
  dataSource.setPassword("");
  return dataSource;
 }

 // <2>
 @Bean
 DataSourceInitializer dataSourceInitializer(DataSource ds,
  @Value("classpath:/schema.sql") Resource schema,
  @Value("classpath:/data.sql") Resource data) {
  DataSourceInitializer init = new DataSourceInitializer();
  init.setDatabasePopulator(new ResourceDatabasePopulator(schema, data));
  init.setDataSource(ds);
  return init;
 }

 // <3>
 @Bean
 JdbcTemplate jdbcTemplate(DataSource ds) {
  return new JdbcTemplate(ds);
 }

 // <4>
 @Bean
 RowMapper<Customer> customerRowMapper() {
  return (rs, i) -> new Customer(rs.getLong("ID"), rs.getString("FIRST_NAME"),
   rs.getString("LAST_NAME"));
 }

 // <5>
 @Bean
 PlatformTransactionManager transactionManager(DataSource ds) {
  return new DataSourceTransactionManager(ds);
 }
}
