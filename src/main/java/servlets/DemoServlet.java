package servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hi/servlets")
class DemoServlet extends HttpServlet {

 private final Log log = LogFactory.getLog(getClass());

 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  throws ServletException, IOException {
  this.log.info("doGet(" + req + ", " + resp + ")");
  resp.setStatus(200);
  resp.setHeader("Content-Type", "application/json");
  resp.getWriter().println("{ \"greeting\" : \"Hello, world\"}");
  resp.getWriter().close();
 }
}
