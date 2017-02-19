package basics.template;

import basics.Customer;
import basics.TransactionalConfiguration;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan
@Import(TransactionalConfiguration.class)
public class TransactionTemplateApplication {

 public static void main(String args[]) {
  new AnnotationConfigApplicationContext(TransactionTemplateApplication.class);
 }

 // <1>
 @Bean
 TransactionTemplate transactionTemplate(PlatformTransactionManager txManager) {
  return new TransactionTemplate(txManager);
 }
}

@Service
class CustomerService {

 private JdbcTemplate jdbcTemplate;
 private TransactionTemplate txTemplate;
 private RowMapper<Customer> customerRowMapper;

 @Autowired
 public CustomerService(TransactionTemplate txTemplate, JdbcTemplate jdbcTemplate,
   RowMapper<Customer> customerRowMapper) {
  this.txTemplate = txTemplate;
  this.jdbcTemplate = jdbcTemplate;
  this.customerRowMapper = customerRowMapper;
 }

 public Customer enableCustomer(Long id) {

  // <2>
  TransactionCallback<Customer> customerTransactionCallback = (
    TransactionStatus transactionStatus) -> {

   String updateQuery = "update CUSTOMER set ENABLED = ? WHERE ID = ?";
   jdbcTemplate.update(updateQuery, Boolean.TRUE, id);

   String selectQuery = "select * from CUSTOMER where ID = ?";
   return jdbcTemplate.queryForObject(selectQuery, customerRowMapper, id);
  };

  // <2>
  Customer customer = txTemplate.execute(customerTransactionCallback);

  LogFactory.getLog(getClass()).info("retrieved customer # " + customer.getId());

  return customer;
 }

}
