package demo;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Named;

// <1>
@Named
public class JerseyConfig extends ResourceConfig {

 public JerseyConfig() {
  this.register(GreetingEndpoint.class); // <2>
  this.register(JacksonFeature.class); // <3>
 }
}
