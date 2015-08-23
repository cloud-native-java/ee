package basics.annotation;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

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
