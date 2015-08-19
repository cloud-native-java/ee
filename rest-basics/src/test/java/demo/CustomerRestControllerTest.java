package demo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static demo.TestUtils.lambaMatcher;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest({"server.port=0"})
public class CustomerRestControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CustomerRepository customerRepository;

    private MockMvc mockMvc;

    private MediaType json = MediaType.parseMediaType("application/json;charset=UTF-8");

    private Customer wellKnownCustomer;

    private String rootPath = "/v1/customers";

    @Before
    public void before() {
        this.wellKnownCustomer =
                this.customerRepository.findById(1L)
                        .orElseGet(() -> this.customerRepository.save(new Customer("Bruce", "Banner")));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void getCustomerById() throws Exception {
        this.mockMvc.perform(get(this.rootPath + "/" + this.wellKnownCustomer.getId()).accept(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$.firstName", is(this.wellKnownCustomer.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(this.wellKnownCustomer.getLastName())));
    }

    @Test
    public void optionsCustomers() throws Throwable {
        this.mockMvc.perform(options(this.rootPath).accept(this.json))
                .andExpect(status().isOk())
                .andExpect(header().string("Allow", notNullValue()));
    }

    @Test
    public void headCustomers() throws Exception {
        this.mockMvc.perform(head(this.rootPath + "/" +
                this.wellKnownCustomer.getId()).contentType(this.json))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getCustomers() throws Exception {
        this.mockMvc.perform(get(this.rootPath)
                .accept(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(json))
                .andExpect(jsonPath("$", hasSize(lambaMatcher("the count should be >= 1", (Integer i) -> i >= 1))));
    }

    @Test
    public void postCustomer() throws Exception {
        String content = "{ \"firstName\": \"Peter\", \"lastName\": \"Parker\" }";
        this.mockMvc.perform(post(this.rootPath)
                .contentType(json)
                .content(content))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()));
    }

    @Test
    public void putCustomer() throws Exception {

        Customer existing = this.customerRepository.findOne(
                this.wellKnownCustomer.getId());

        String fn = "Peter", ln = "Parker";

        String content = "{ \"id\": \"" + existing.getId() + "\", \"firstName\": \"" + fn + "\", \"lastName\": \"" + ln + "\" }";
        this.mockMvc.perform(put(this.rootPath + "/" + existing.getId())
                .contentType(json).content(content))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()));

        Customer updated = this.customerRepository.findOne(
                this.wellKnownCustomer.getId());
        assertNotEquals(existing.getFirstName(), updated.getFirstName());
        assertNotEquals(existing.getLastName(), updated.getLastName());
        assertEquals(updated.getLastName(), ln);
        assertEquals(updated.getFirstName(), fn);

        this.mockMvc.perform(put(this.rootPath + "/" + 0)
                .content(content)
                .contentType(this.json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteCustomer() throws Exception {
        this.mockMvc.perform(delete(this.rootPath + "/" + this.wellKnownCustomer.getId()).contentType(this.json))
                .andExpect(status().isNoContent());
        this.customerRepository.findById(this.wellKnownCustomer.getId())
                .ifPresent(x -> fail());

        this.mockMvc.perform(delete(this.rootPath + '/' + 0))
                .andExpect(status().isNotFound());
    }

}
