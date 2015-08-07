package demo;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/v1/customers")
public class CustomerRestController {

    private final CustomerRepository customerRepository;

    @Autowired
    CustomerRestController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    ResponseEntity<?> options() {
        return ResponseEntity
                .ok()
                .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD,
                        HttpMethod.OPTIONS, HttpMethod.PUT, HttpMethod.DELETE)
                .build();
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get all Customers", notes = "Gets all Customers")
    @ApiResponses({@ApiResponse(code = 200, message = "returns all the available customer records")})
    ResponseEntity<Collection<Customer>> getCollection() {
        return ResponseEntity.ok(this.customerRepository.findAll());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    ResponseEntity<Customer> get(@PathVariable Long id) {
        return this.customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Customer> post(@RequestBody Customer c) {

        Customer customer = this.customerRepository.save(
                new Customer(c.getFirstName(), c.getLastName()));
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}")
                .buildAndExpand(customer.getId())
                .toUri();
        return ResponseEntity.created(uri).body(customer);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    ResponseEntity<?> delete(@PathVariable Long id) {
        return this.customerRepository.findById(id).
                map(c -> {
                    customerRepository.delete(c);
                    return ResponseEntity.noContent().build();
                })
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    ResponseEntity<?> head(@PathVariable Long id) {
        return this.customerRepository.findById(id)
                .map(exists -> ResponseEntity.noContent().build())
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    ResponseEntity<Customer> put(@PathVariable Long id, @RequestBody Customer c) {

        return this.customerRepository.findById(id)
                .map(existing -> {

                    Customer customer = this.customerRepository.save(new Customer(existing.getId(),
                            c.getFirstName(), c.getLastName()));
                    URI selfLink = URI.create(ServletUriComponentsBuilder.fromCurrentRequest().toUriString());
                    return ResponseEntity.created(selfLink).body(customer);
                })
                .orElseThrow(() -> new CustomerNotFoundException(id));


    }
}

