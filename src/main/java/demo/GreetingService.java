package demo;

import org.springframework.jms.core.JmsTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

@Named
@javax.transaction.Transactional
// <1>
public class GreetingService {

	@Inject
	private JmsTemplate jmsTemplate; // <2>

	@PersistenceContext
	// <3>
	private EntityManager entityManager;

	public void createGreeting(String name, boolean fail) { // <4>
		Greeting greeting = new Greeting(name);
		this.entityManager.persist(greeting);
		this.jmsTemplate.convertAndSend("greetings", greeting);
		if (fail) {
			throw new RuntimeException("simulated error");
		}
	}

	public void createGreeting(String name) {
		this.createGreeting(name, false);
	}

	public Collection<Greeting> findAll() {
		return this.entityManager.createQuery(
				"select g from " + Greeting.class.getName() + " g", Greeting.class)
				.getResultList();
	}

	public Greeting find(Long id) {
		return this.entityManager.find(Greeting.class, id);
	}
}
