package client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This demonstrates setting up a deeper HAL-based microservice and then
 * using {@link RestTemplate} and {@link Traverson} to interact
 * with the resources
 */
@SpringBootApplication
public class Application {

    @Value("${application-domain}")
    void setApplicationDomain(String host) {
        this.baseUri = URI.create(host);
    }

    private URI baseUri;

    private Log log = LogFactory.getLog(getClass());

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // @tag::rest-mvc-configuratino[]
    @Configuration
    static class SimpleRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {

        @Override
        protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
            config.exposeIdsFor(Movie.class, Actor.class);
        }
    }
    // @end::rest-mvc-configuratino[]


    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    // @tag::rest-template[]
    @Bean
    RestTemplate restTemplate() {
        String nl = System.getProperty("line.separator");
        ClientHttpRequestInterceptor interceptor = (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            String bodyStr = body != null && body.length > 0 ? String.format("having request body %s%s%s", nl, new String(body), nl) : "";

            log.info(String.format("request to URI %s with HTTP verb '%s' %s",
                    request.getURI(), request.getMethod().toString(), bodyStr));

            return execution.execute(request, body);
        };

        RestTemplate simpleRestTemplate = new RestTemplate();
        simpleRestTemplate.getInterceptors().add(interceptor);
        return simpleRestTemplate;
    }
    // @end::rest-template[]

    // @tag::traverson[]
    @Bean
    Traverson traverson(RestTemplate restTemplate) {
        Traverson traverson = new Traverson(baseUri, MediaTypes.HAL_JSON);
        traverson.setRestOperations(restTemplate);
        return traverson;
    }
    // @end::traverson[]

    @Bean
    CommandLineRunner clientRunner(TransactionTemplate transactionTemplate,
                             MovieRepository mr,
                             ActorRepository ar,
                             RestTemplate restTemplate,
                             Traverson traverson) {
        return args -> {

            // <1>
            transactionTemplate.execute(tx ->
                    Arrays.asList(
                            ("Cars(Owen Wilson,Paul Newman,Bonnie Hunt); " +
                                    "Batman(Michael Keaton,Jack Nicholson); " +
                                    "Lost in Translation (Bill Murray)").split(";"))
                            .stream()
                            .map(String::trim)
                            .map(i -> {
                                Matcher matcher = Pattern.compile("(.*?)\\((.*?)\\)").matcher(i);
                                Assert.isTrue(matcher.matches());
                                Movie movie = mr.save(new Movie(matcher.group(1)));
                                Arrays.asList(matcher.group(2).split(","))
                                        .stream()
                                        .map(String::trim)
                                        .forEach(a -> {
                                            Actor actor = ar.save(new Actor(a.trim(), movie));
                                            movie.actors.add(ar.findOne(actor.id));
                                            mr.save(movie);
                                        });
                                return mr.findOne(movie.id);
                            }).collect(Collectors.toList()));


            //  @tag::rest-template-client[]
            URI uriOfNewMovie = restTemplate.postForLocation(baseUri.toString() + "movies",
                    new Movie("Forest Gump"));

            log.info("the new movie lives at " + uriOfNewMovie);

            log.info("\t..read as a Map.class: " + restTemplate.getForObject(uriOfNewMovie, Map.class));

            log.info("\t..read as a Movie.class: " + restTemplate.getForObject(uriOfNewMovie, Movie.class));

            log.info("\t..read as a ResponseEntity<Movie>: " + restTemplate.getForEntity(uriOfNewMovie, Movie.class));
            // @end::rest-template-client[]

            // @tag::traverson-client[]
            String nameOfMovie = "Cars";
            Resources<Resource<Actor>> actorResources = traverson.follow("actors", "search", "by-movie")
                    .withTemplateParameters(Collections.singletonMap("movie", nameOfMovie))
                    .toObject(new ParameterizedTypeReference<Resources<Resource<Actor>>>() {
                    });
            actorResources.forEach(System.out::println);
            // @end::traverson-client[]
        };
    }
}
