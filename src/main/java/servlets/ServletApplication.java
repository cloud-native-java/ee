package servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@ServletComponentScan
@SpringBootApplication
public class ServletApplication {

	public static void main(String args[]) {
		SpringApplication.run(ServletApplication.class, args);
	}

	@Bean
	FilterRegistrationBean filter() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean(new LoggingFilter());
		registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registrationBean;
	}
}

@RestController
class GreetingsRestController {

	@GetMapping("/hi/mvc")
	Map<String, Object> hi() {
		return Collections.singletonMap("greetings", "Hello, world!");
	}
}

class LoggingFilter implements Filter {

	private final Log log = LogFactory.getLog(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.log.info("init()");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		this.log.info("before doFilter(" + req + ", " + resp + ")");
		chain.doFilter(req, resp);
		this.log.info("after doFilter(" + req + ", " + resp + ")");
	}

	@Override
	public void destroy() {
		log.info("destroy()");
	}
}

@WebServlet("/hi/servlets")
class DemoServlet extends HttpServlet {

	private final Log log = LogFactory.getLog(getClass());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.log.info("doGet(" + req + ", " + resp + ")");
		resp.setStatus(200);
		resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE); // using Spring `HttpHeaders` and `MediaType` to cheat a bit
		resp.getWriter().println("{ \"greeting\" : \"Hello, world\"}");
		resp.getWriter().close();
	}
}