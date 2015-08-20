package basics;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;


@Configuration
@EnableTransactionManagement
public class TransactionalApplication {

    public static void main(String args[]) {
        new AnnotationConfigApplicationContext(
                TransactionTemplateApplication.class);
    }

    @Bean
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("customers")
                .build();
    }

    // @tag::transactional[]
    @Service
    public static class CustomerService {

        private JdbcTemplate jdbcTemplate;
        private RowMapper<Customer> customerRowMapper;

        @Autowired
        public CustomerService(JdbcTemplate jdbcTemplate,
                               RowMapper<Customer> customerRowMapper) {
            this.jdbcTemplate = jdbcTemplate;
            this.customerRowMapper = customerRowMapper;
        }

        // @org.springframework.transaction.annotation.Transactional <1>
        // @javax.ejb.TransactionAttribute <2>
        @javax.transaction.Transactional  // <3>
        public Customer enableCustomer(Long id) {

            String updateQuery = "update CUSTOMER set ENABLED =  ? WHERE ID = ?";
            jdbcTemplate.update(updateQuery, Boolean.TRUE, id);

            String selectQuery = "select * from CUSTOMER where ID = ?";
            Customer customer = jdbcTemplate.queryForObject(selectQuery, customerRowMapper, id);

            LogFactory.getLog(getClass()).info("retrieved customer # " + customer.getId());
            return customer;
        }
    }
    // @end::transactional[]

    public static class Customer {
        private Long id;
        private String firstName, lastName;

        public Customer(Long id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getId() {

            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    PlatformTransactionManager platformTransactionManager(
            DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    TransactionTemplate transactionTemplate(
            PlatformTransactionManager txManager) {
        return new TransactionTemplate(txManager);
    }

    @Bean
    InitializingBean runner(RowMapper<Customer> customerRowMapper,
                            CustomerService customerService, JdbcTemplate jdbcTemplate) {
        return () -> {
            jdbcTemplate.execute("create table CUSTOMER(ID serial, FIRST_NAME varchar, LAST_NAME varchar , ENABLED int(1))");
            jdbcTemplate.update("insert into CUSTOMER( FIRST_NAME , LAST_NAME, ENABLED) VALUES ( ? , ?, ?)",
                    "Dave", "Syer", false);
            jdbcTemplate.query("select  * from CUSTOMER", customerRowMapper);
            customerService.enableCustomer(1L);
        };
    }

    @Bean
    RowMapper<Customer> customerRowMapper() {
        return (resultSet, i) ->
                new Customer(
                        resultSet.getLong("ID"),
                        resultSet.getString("FIRST_NAME"),
                        resultSet.getString("LAST_NAME"));
    }

}

