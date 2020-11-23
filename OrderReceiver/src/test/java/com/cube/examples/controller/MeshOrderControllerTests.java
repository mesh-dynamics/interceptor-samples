package com.cube.examples.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.JacksonJsonParser;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.md.junit.AutoConfigureMeshDContext;
import io.md.junit.MeshDParameterResolver;
import io.md.junit.MeshDRequest;
import io.md.junit.MeshDResponse;
import io.md.junit.MeshDTestExecutionListener;
import io.md.junit.MeshTestCaseId;

import com.cube.examples.AbstractTest;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestExecutionListeners(value = {
    MeshDTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class
})
@ExtendWith(MeshDParameterResolver.class)
@AutoConfigureMeshDContext(
    meshDHost = "demo.dev.cubecorp.io",
    authToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNZXNoREFnZW50VXNlckBjdWJlY29ycC5pbyIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJ0eXBlIjoicGF0IiwiY3VzdG9tZXJfaWQiOjMsImlhdCI6MTU4OTgyODI4NiwiZXhwIjoxOTA1MTg4Mjg2fQ.Xn6JTEIAi58it6iOSZ0G7u2waK6a_c-Elpk_cpWsK9s",
    goldenNames = {"cicd-2715", "negatvie_collection"},
    app = "springboot_demo"
    )
class MeshOrderControllerTests extends AbstractTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private String obtainAccessToken(String username, String password) throws Exception {

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("username", username);
    params.add("password", password);

    ResultActions result
        = mvc.perform(post("/oauth/token")
        .params(params)
        .with(httpBasic("order-receiver","secret"))
        .accept("application/json;charset=UTF-8"))
        .andExpect(status().isOk());

    String resultString = result.andReturn().getResponse().getContentAsString();

    JacksonJsonParser jsonParser = new JacksonJsonParser();
    return jsonParser.parseMap(resultString).get("access_token").toString();
  }

  @BeforeEach
  public void setUp() throws URISyntaxException {
    super.setUp();
  }


  @Test
  @MeshTestCaseId(traceIds = {"2b02275d96575", "495409f565cf4c10"}, path = "orders/getOrders/")
  public void testGetOrders(MeshDRequest[] requests, MeshDResponse[] responses) throws Exception {
    String accessToken = obtainAccessToken("admin@admin.com", "pwd");

    //construct the request to the controller using the data fetch from the MeshD stored captured request
    String body = requests[0].getBody().toString();;
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
        .get(requests[0].getPath())
        .header("Authorization", "Bearer " + accessToken))
        .andReturn();
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    //write assertion based on the captured response data pulled from MeshD server
    assertEquals(responses[0].getBody(), mvcResult.getResponse().getContentAsString());
  }

  @Test
  @MeshTestCaseId(traceIds = {"65db02772e5ecad9", "474aac87a50101f2"}, path = "orders/postOrder/")
  public void testPlaceOrders(MeshDRequest[] requests, MeshDResponse[] responses) throws Exception {

    String accessToken = obtainAccessToken("admin@admin.com", "pwd");

    //construct the request to the controller using the data fetch from the MeshD stored captured request
    String body = requests[0].getBody().toString();
    MvcResult mvcResult = mvc.perform(post(requests[0].getPath())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(body)
        .header("Authorization", "Bearer " + accessToken)
        .header("md-trace-id", "474aac87a50101f2:474aac87a50101f2:0:1"))
        .andReturn();
    int status = mvcResult.getResponse().getStatus();

    //Write your own assertion rules
    assertEquals(201, status);
  }
}
