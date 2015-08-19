package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Spring REST Docs example (Kenny can help here!)
 */

@RestController
@RequestMapping(value = "/v2", produces = "application/hal+json")
public class CustomerHypermediaRestController {

    @Autowired
    private CustomerResourceAssembler customerResourceAssembler;

    private final CustomerRepository customerRepository;

    @Autowired
    CustomerHypermediaRestController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    ResponseEntity<Resources<Object>> root() {
        Resources<Object> objects = new Resources<>(Collections.emptyList());
        URI uri = MvcUriComponentsBuilder.fromMethodCall(MvcUriComponentsBuilder.on(getClass()).getCollection()).build().toUri();
        Link link = new Link(uri.toString(), "customers");
        objects.add(link);
        return ResponseEntity.ok(objects);

    }

    @RequestMapping(value = "/customers", method = RequestMethod.OPTIONS)
    ResponseEntity<?> options() {
        return ResponseEntity
                .ok()
                .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD,
                        HttpMethod.OPTIONS, HttpMethod.PUT, HttpMethod.DELETE)
                .build();
    }

    @RequestMapping(value = "/customers", method = RequestMethod.GET)
    ResponseEntity<Resources<Resource<Customer>>> getCollection() {
        List<Resource<Customer>> collect = this.customerRepository.findAll()
                .stream()
                .map(customerResourceAssembler::toResource)
                .collect(Collectors.<Resource<Customer>>toList());
        Resources<Resource<Customer>> resources = new Resources<>(collect);
        URI self =ServletUriComponentsBuilder.fromCurrentRequest( ).build().toUri();
        resources.add(new Link(self.toString(), "self"));
        return ResponseEntity.ok(resources);
    }

    @RequestMapping(value = "/customers/{id}", method = RequestMethod.GET)
    ResponseEntity<Resource<Customer>> get(@PathVariable Long id) {
        return this.customerRepository.findById(id)
                .map(c -> ResponseEntity.ok(this.customerResourceAssembler.toResource(c)))
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(value = "/customers", method = RequestMethod.POST)
    ResponseEntity<Resource<Customer>> post(@RequestBody Customer c) {

        Customer customer = this.customerRepository.save(
                new Customer(c.getFirstName(), c.getLastName()));
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/customers/{id}")
                .buildAndExpand(customer.getId())
                .toUri();
        return ResponseEntity.created(uri).body(
                this.customerResourceAssembler.toResource(customer));
    }

    @RequestMapping(value = "/customers/{id}", method = RequestMethod.DELETE)
    ResponseEntity<?> delete(@PathVariable Long id) {
        return this.customerRepository.findById(id).
                map(c -> {
                    customerRepository.delete(c);
                    return ResponseEntity.noContent().build();
                })
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(value = "/customers/{id}", method = RequestMethod.HEAD)
    ResponseEntity<?> head(@PathVariable Long id) {
        return this.customerRepository.findById(id)
                .map(exists -> ResponseEntity.noContent().build())
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(value = "/customers/{id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<Customer>> put(@PathVariable Long id, @RequestBody Customer c) {
        Customer customer = this.customerRepository.save(new Customer(id, c.getFirstName(), c.getLastName()));
        Resource<Customer> customerResource = this.customerResourceAssembler.toResource(customer);
        URI selfLink = URI.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
        return ResponseEntity.created(selfLink).body(customerResource);
    }
}

@Component
class CustomerResourceAssembler implements ResourceAssembler<Customer, Resource<Customer>> {

    @Override
    public Resource<Customer> toResource(Customer customer) {
        Resource<Customer> customerResource = new Resource<>(customer);
        URI photoURI = MvcUriComponentsBuilder
                .fromMethodCall(MvcUriComponentsBuilder.on(
                        CustomerProfilePhotoRestController.class).read(customer.getId()))
                .buildAndExpand()
                .toUri();

        URI selfURI = MvcUriComponentsBuilder.fromMethodCall(
                MvcUriComponentsBuilder.on(CustomerHypermediaRestController.class).get(customer.getId()))
                .buildAndExpand()
                .toUri();


        customerResource.add(new Link(selfURI.toString(), "self"));
        customerResource.add(new Link(photoURI.toString(), "profile-photo"));
        return customerResource;
    }
}