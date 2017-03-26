package servlets;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@ServletComponentScan
public class ServletConfiguration {

 @Bean
 FilterRegistrationBean filter() {
  FilterRegistrationBean registrationBean = new FilterRegistrationBean(
   new LoggingFilter());
  registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
  return registrationBean;
 }
}
