package servlets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a
 * href="mailto:josh@joshlong.com">Josh
 * Long</a>
 */
@RestController
class GreetingsRestController {

 @GetMapping("/hi/mvc")
 Map<String, Object> hi() {
  return Collections.singletonMap("greetings", "Hello, world!");
 }
}
