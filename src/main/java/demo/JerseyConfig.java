package demo;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Named;

@Named
// <1>
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		this.register(GreetingEndpoint.class); // <2>
		this.register(JacksonFeature.class); // <3>
	}
}
