package basics.template;

import basics.Customer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TransactionTemplateApplication.class)
public class TransactionTemplateApplicationTest {

    @Autowired
    private RowMapper<Customer> customerRowMapper;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void contextLoaded() throws Exception {
        String enabledQuery = "select * from CUSTOMER where ENABLED=1";
        assertEquals(jdbcTemplate.query(enabledQuery, customerRowMapper).size(), 0);
        customerService.enableCustomer(1L);
        assertEquals(jdbcTemplate.query(enabledQuery, customerRowMapper).size(), 1);
    }

    @Before
    public void setUp() throws Exception {
        jdbcTemplate.query("select  * from CUSTOMER", customerRowMapper);
    }
}