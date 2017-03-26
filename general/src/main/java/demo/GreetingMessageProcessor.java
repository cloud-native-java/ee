package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;

import javax.inject.Named;
import javax.jms.JMSException;

// <1>
@Named
public class GreetingMessageProcessor {

 private Log log = LogFactory.getLog(getClass());

 @JmsListener(destination = "greetings")
 public void processGreeting(Greeting greeting) throws JMSException {
  log.info("received message: " + greeting);
 }
}
