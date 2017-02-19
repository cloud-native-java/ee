package demo;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Named
// <1>
@Path("/hello")
// <2>
@Produces({ MediaType.APPLICATION_JSON })
// <3>
public class GreetingEndpoint {

 @Inject
 private GreetingService greetingService;

 @POST
 // <4>
 public void post(@QueryParam("name") String name) { // <5>
  this.greetingService.createGreeting(name);
 }

 @GET
 @Path("/{id}")
 public Greeting get(@PathParam("id") Long id) {
  return this.greetingService.find(id);
 }
}
