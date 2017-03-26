package servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author <a
 * href="mailto:josh@joshlong.com">Josh
 * Long</a>
 */
class LoggingFilter implements Filter {

 private final Log log = LogFactory.getLog(getClass());

 @Override
 public void init(FilterConfig filterConfig) throws ServletException {
  this.log.info("init()");
 }

 @Override
 public void doFilter(ServletRequest req, ServletResponse resp,
  FilterChain chain) throws IOException, ServletException {
  this.log.info("before doFilter(" + req + ", " + resp + ")");
  chain.doFilter(req, resp);
  this.log.info("after doFilter(" + req + ", " + resp + ")");
 }

 @Override
 public void destroy() {
  log.info("destroy()");
 }
}
