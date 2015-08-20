package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.payload.FieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.RequestDispatcher;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.RestDocumentation.document;
import static org.springframework.restdocs.RestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class HypermediaApiDocumentation {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration()).build();
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(documentationConfiguration()
                        .uris()
                        .withScheme("https")
                        .withHost("localhost")
                        .withPort(8443)
                        .and().snippets()
                        .withEncoding("ISO-8859-1"))
                .build();

    }

    @Test
    public void errorExample() throws Exception {
        this.mockMvc
                .perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI,
                                "/customers")
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE,
                                "The customer 'http://localhost:8443/v2/customers/123' does not exist"))
                .andDo(print()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", is("Bad Request")))
                .andExpect(jsonPath("timestamp", is(notNullValue())))
                .andExpect(jsonPath("status", is(400)))
                .andExpect(jsonPath("path", is(notNullValue())))
                .andDo(document("error-example")
                        .withResponseFields(
                                fieldWithPath("error").description("The HTTP error that occurred, e.g. `Bad Request`"),
                                fieldWithPath("message").description("A description of the cause of the error"),
                                fieldWithPath("path").description("The path to which the request was made"),
                                fieldWithPath("status").description("The HTTP status code, e.g. `400`"),
                                fieldWithPath("timestamp").description("The time, in milliseconds, at which the error occurred")));
    }

    @Test
    public void customersListExample() throws Exception {

        this.mockMvc.perform(get("/v2/customers"))
                .andExpect(status().isOk())
                .andDo(document("customers-list-example")
                        .withResponseFields(
                                fieldWithPath("_links.self.href").description("A link to the Customers"),
                                fieldWithPath("_links.self.templated").description("is the URI templated"),
                                fieldWithPath("_embedded.customerList").description("An array of <<resources-customer, Customer resources>>")));
    }

    @Test
    public void customersCreateExample() throws Exception {
        Map<String, String> customer = new HashMap<String, String>();
        customer.put("firstName", "Chris");
        customer.put("lastName", "Richardson");

        this.mockMvc
                .perform(
                        post("/v2/customers").contentType(MediaTypes.HAL_JSON).content(
                                this.objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andDo(document("customers-create-example")
                        .withRequestFields(
                                fieldWithPath("firstName").description("The first name of the customer"),
                                fieldWithPath("lastName").description("The last name of the customer")));

    }

    @Test
    public void customerGetExample() throws Exception {
        Map<String, String> customer = new HashMap<String, String>();
        customer.put("firstName", "Jez");
        customer.put("lastName", "Humble");

        String customerLocation = this.mockMvc
                .perform(
                        post("/v2/customers").contentType(MediaTypes.HAL_JSON).content(
                                this.objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated()).andReturn().getResponse()
                .getHeader("Location");

        this.mockMvc.perform(get(customerLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("firstName", is(customer.get("firstName"))))
                .andExpect(jsonPath("lastName", is(customer.get("lastName"))))
                .andExpect(jsonPath("_links.self.href", is(customerLocation)))
                .andDo(document("customer-get-example")
                        .withLinks(
                                linkWithRel("self").description("This <<resources-customer,customer>>"),
                                linkWithRel("profile-photo").description("The <<resources-profile-photo,profile-photo>>"))
                        .withResponseFields(
                                fieldWithPath("id").description("The id of this customer"),
                                fieldWithPath("firstName").description("The first name of the customer"),
                                fieldWithPath("lastName").description("The last name of the customer"),
                                fieldWithPath("_links").description("<<resources-customer-links,Links>> to other resources")));

    }

    @Test
    public void customerUpdateExample() throws Exception {
        Map<String, String> customer = new HashMap<String, String>();
        customer.put("firstName", "Martin");
        customer.put("lastName", "Fowler");

        String customerLocation = this.mockMvc
                .perform(
                        post("/v2/customers").contentType(MediaTypes.HAL_JSON).content(
                                this.objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated()).andReturn().getResponse()
                .getHeader("Location");

        this.mockMvc.perform(get(customerLocation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("firstName", is(customer.get("firstName"))))
                .andExpect(jsonPath("lastName", is(customer.get("lastName"))))
                .andExpect(jsonPath("_links.self.href", is(customerLocation)));

        Map<String, String> customerUpdate = new HashMap<String, String>();
        customerUpdate.put("firstName", "Martin");
        customerUpdate.put("lastName", "Fowler");

        this.mockMvc.perform(
                put(customerLocation).contentType(MediaTypes.HAL_JSON).content(
                        this.objectMapper.writeValueAsString(customerUpdate)))
                .andExpect(status().isCreated())
                .andDo(document("customer-update-example")
                        .withRequestFields(
                                fieldWithPath("firstName").description("The first name of the customer").type(FieldType.STRING).optional(),
                                fieldWithPath("lastName").description("The last name of the customer").type(FieldType.STRING).optional()));
    }
}
