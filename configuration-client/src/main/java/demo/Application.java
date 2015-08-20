package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

// <1>
@RestController
@RefreshScope
class ProjectNameRestController {

	// <2>
	@Value("${configuration.projectName}")
	private String projectName;

	@RequestMapping("/project-name")
	String projectName() {
		return this.projectName;
	}
}