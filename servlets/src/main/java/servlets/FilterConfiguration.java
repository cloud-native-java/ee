package servlets;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.time.Instant;

@Configuration
public class FilterConfiguration {

 @Bean
 FilterRegistrationBean filter() {
  LoggingFilter filter = new LoggingFilter(); // <1>
  FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
  registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
  // <2>
  registrationBean.addInitParameter("instant-initialized", Instant.now()
   .toString());
  return registrationBean;
 }
}
