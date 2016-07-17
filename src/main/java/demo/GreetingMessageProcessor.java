package demo;

import org.springframework.jms.annotation.JmsListener;

import javax.inject.Named;
import javax.jms.JMSException;

@Named
// <1>
public class GreetingMessageProcessor {

	@JmsListener(destination = "greetings")
	public void processGreeting(Greeting greeting) throws JMSException {
		System.out.println("received message: " + greeting);
	}
}
