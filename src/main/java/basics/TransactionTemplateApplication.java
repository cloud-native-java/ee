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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
public class TransactionTemplateApplication {

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

    // @tag::transaction-template[]
    @Service
    public static class CustomerService {

        private JdbcTemplate jdbcTemplate;
        private TransactionTemplate txTemplate;
        private RowMapper<Customer> customerRowMapper;

        @Autowired
        public CustomerService(TransactionTemplate txTemplate,
                               JdbcTemplate jdbcTemplate,
                               RowMapper<Customer> customerRowMapper) {
            this.txTemplate = txTemplate;
            this.jdbcTemplate = jdbcTemplate;
            this.customerRowMapper = customerRowMapper;
        }

        public Customer enableCustomer(Long id) {

            Customer customer = txTemplate.execute((TransactionStatus transactionStatus) -> {

                String updateQuery = "update CUSTOMER set ENABLED = ? WHERE ID = ?";
                jdbcTemplate.update(updateQuery, Boolean.TRUE, id);

                String selectQuery = "select * from CUSTOMER where ID = ?";
                return jdbcTemplate.queryForObject(selectQuery, customerRowMapper, id);
            });

            LogFactory.getLog(getClass()).info("retrieved customer # " + customer.getId());

            return customer;
        }

    }
    // @end::transaction-template[]

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
