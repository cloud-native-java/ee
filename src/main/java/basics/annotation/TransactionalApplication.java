package basics.annotation;

import basics.Customer;
import basics.TransactionalConfiguration;
import basics.template.TransactionTemplateApplication;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@ComponentScan
@Import(TransactionalConfiguration.class)
@EnableTransactionManagement // <1>
public class TransactionalApplication {

    public static void main(String args[]) {
        new AnnotationConfigApplicationContext(TransactionTemplateApplication.class);
    }
}

@Service
class CustomerService {

    private JdbcTemplate jdbcTemplate;
    private RowMapper<Customer> customerRowMapper;

    @Autowired
    public CustomerService(JdbcTemplate jdbcTemplate,
                           RowMapper<Customer> customerRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.customerRowMapper = customerRowMapper;
    }

    // @org.springframework.transaction.annotation.Transactional <2>
    // @javax.ejb.TransactionAttribute <3>
    @javax.transaction.Transactional  // <4>
    public Customer enableCustomer(Long id) {

        String updateQuery = "update CUSTOMER set ENABLED = ? WHERE ID = ?";
        jdbcTemplate.update(updateQuery, Boolean.TRUE, id);

        String selectQuery = "select * from CUSTOMER where ID = ?";
        Customer customer = jdbcTemplate.queryForObject(selectQuery, customerRowMapper, id);

        LogFactory.getLog(getClass()).info("retrieved customer # " + customer.getId());
        return customer;
    }
}
