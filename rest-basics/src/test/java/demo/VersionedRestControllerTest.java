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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest({"server.port=0"})
public class VersionedRestControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MediaType v1MediaType = MediaType.parseMediaType(VersionedRestController.V1_MEDIA_TYPE_VALUE);

    private MediaType v2MediaType = MediaType.parseMediaType(VersionedRestController.V2_MEDIA_TYPE_VALUE);

    @Before
    public void before() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void versioningApiWithPathVariable() throws Throwable {
        this.mockMvc.perform(get("/api/v2/hi"))
                .andExpect(jsonPath("$.version", is("v2")))
                .andExpect(jsonPath("$.how", is("path-variable")));

        this.mockMvc.perform(get("/api/v1/hi"))
                .andExpect(jsonPath("$.version", is("v1")))
                .andExpect(jsonPath("$.how", is("path-variable")));
    }

    @Test
    public void versioningApiWithContentNegotiation() throws Throwable {
        this.mockMvc.perform(get("/api/hi").accept(this.v2MediaType))
                .andExpect(jsonPath("$.version", is("v2")))
                .andExpect(jsonPath("$.how", is("content-negotiation")));

        this.mockMvc.perform(get("/api/hi").accept(this.v1MediaType))
                .andExpect(jsonPath("$.version", is("v1")))
                .andExpect(jsonPath("$.how", is("content-negotiation")));
    }

    @Test
    public void versioningApiWithHeader() throws Throwable {
        this.mockMvc.perform(get("/api/hi").header("X-Api-Version", "v2"))
                .andExpect(jsonPath("$.version", is("v2")))
                .andExpect(jsonPath("$.how", is("header")));

        this.mockMvc.perform(get("/api/hi").header("X-Api-Version", "v1"))
                .andExpect(jsonPath("$.version", is("v1")))
                .andExpect(jsonPath("$.how", is("header")));
    }
}
