package servlets;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class ServletConfiguration {

// @Bean
 FilterRegistrationBean filter() {
  LoggingFilter filter = new LoggingFilter();
  FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
  registrationBean.addInitParameter("instant-initialized", Instant.now()
   .toString());
//  registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
  return registrationBean;
 }
}
