package com.cube.examples;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

import io.cube.spring.ingress.TracingFilter;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrderReceiverApplication.class)
@ContextConfiguration
@WebAppConfiguration
//@ActiveProfiles("replay")
public abstract class AbstractTest {

  static {
    System.setProperty("io.md.app", "springboot_demo");
    System.setProperty("io.md.customer", "CubeCorp");
    System.setProperty("io.md.instance", "prod");
    System.setProperty("io.md.servicename", "order");
    System.setProperty("io.md.service.endpoint", "https://demo.dev.cubecorp.io/api/");
    System.setProperty("io.md.authtoken", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNZXNoREFnZW50VXNlckBjdWJlY29ycC5pbyIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoicGF0IiwiY3VzdG9tZXJfaWQiOjMsImlhdCI6MTU4OTgyODI4NiwiZXhwIjoxOTA1MTg4Mjg2fQ.Xn6JTEIAi58it6iOSZ0G7u2waK6a_c-Elpk_cpWsK9s");
  }


  protected MockMvc mvc;
  @Autowired
  WebApplicationContext webApplicationContext;

  @Autowired TracingFilter tracingFilter;

  protected void setUp() throws URISyntaxException {
    //mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilter(springSecurityFilterChain).build();
    mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .addFilter(tracingFilter).apply(springSecurity()).build();
  }
  protected String mapToJson(Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(obj);
  }
  protected <T> T mapFromJson(String json, Class<T> clazz)
      throws JsonParseException, JsonMappingException, IOException {

    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, clazz);
  }
}
