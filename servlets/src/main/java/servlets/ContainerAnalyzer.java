package servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//@formatter:off
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded
        .EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded
        .jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded
        .tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
//@formatter:on
import org.springframework.stereotype.Component;

@Component
class ContainerAnalyzer implements EmbeddedServletContainerCustomizer {

 private final Log log = LogFactory.getLog(getClass());

 @Override
 public void customize(ConfigurableEmbeddedServletContainer c) {

  this.log.info("inside " + getClass().getName());

  // <1>
  AbstractEmbeddedServletContainerFactory base = AbstractEmbeddedServletContainerFactory.class
   .cast(c);
  this.log.info("the container's running on port " + base.getPort());
  this.log.info("the container's context-path is " + base.getContextPath());

  // <2>
  if (UndertowEmbeddedServletContainerFactory.class.isAssignableFrom(c
   .getClass())) {
   UndertowEmbeddedServletContainerFactory undertow = UndertowEmbeddedServletContainerFactory.class
    .cast(c);
   undertow.getDeploymentInfoCustomizers().forEach(
    dic -> log.info("undertow deployment info customizer " + dic));
   undertow.getBuilderCustomizers().forEach(
    bc -> log.info("undertow builder customizer " + bc));
  }

  // <3>
  if (TomcatEmbeddedServletContainerFactory.class
   .isAssignableFrom(c.getClass())) {
   TomcatEmbeddedServletContainerFactory tomcat = TomcatEmbeddedServletContainerFactory.class
    .cast(c);
   tomcat.getTomcatConnectorCustomizers().forEach(
    cc -> log.info("tomcat connector customizer " + cc));
   tomcat.getTomcatContextCustomizers().forEach(
    cc -> log.info("tomcat context customizer " + cc));
  }

  // <4>
  if (JettyEmbeddedServletContainerFactory.class.isAssignableFrom(c.getClass())) {
   JettyEmbeddedServletContainerFactory jetty = JettyEmbeddedServletContainerFactory.class
    .cast(c);
   jetty.getServerCustomizers().forEach(
    cc -> log.info("jetty server customizer " + cc));
  }
 }

}
