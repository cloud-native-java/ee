package demo;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Logger;

@Named
public class GreetingServiceClient {

 @Inject
 private GreetingService greetingService;

 @PostConstruct
 // <1>
 public void afterPropertiesSet() throws Exception {
  greetingService.createGreeting("Phil");
  greetingService.createGreeting("Dave");
  try {
   greetingService.createGreeting("Josh", true);
  }
  catch (RuntimeException re) {
   Logger.getLogger(Application.class.getName()).info("caught exception...");
  }
  greetingService.findAll().forEach(System.out::println);
 }
}
