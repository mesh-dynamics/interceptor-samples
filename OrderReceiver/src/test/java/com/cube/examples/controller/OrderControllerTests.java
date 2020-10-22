package com.cube.examples.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.util.JacksonJsonParser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.cube.examples.AbstractTest;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderControllerTests extends AbstractTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private JsonNode getMeshRequestData(String reqId) throws URISyntaxException, IOException {
    String requestBody = "{\"customerId\":\"CubeCorp\","
        + "\"app\":\"springboot_demo\","
        + "\"eventTypes\":[],"
        + "\"services\":[],"
        + "\"traceIds\":[],"
        + "\"reqIds\":[\""
        + reqId
        + "\"],"
        + "\"paths\":[]}";

    final String url = "https://demo.dev.cubecorp.io" + "/api/cs/getEvents";
    URI uri = new URI(url);

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    //The token used here is a short lived. It has be updated with appropriate token to make this work
    headers.set("Authorization",
        "Bearer eyJhbGciOjJIUzI1NiJ9.eyJzdWIiOiJkZW1vQGN1YmVjb3JwLmlvIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTYwMzI1MzczNywiZXhwIjoxNjAzMzQwMTM3fQ.4U0AZyZhpEEy6CyNILDRiBSBY3Wyt0N3WXe78fDd_6Q");
    headers.set("content-type","application/json");

    HttpEntity entity = new HttpEntity(requestBody, headers);
    ResponseEntity<String> responseEntityStr = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    JsonNode root = objectMapper.readTree(responseEntityStr.getBody());
    return objectMapper.readTree(responseEntityStr.getBody()).get("objects");
  }

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

  @Before
  public void setUp() {
    super.setUp();
  }

  @Test
  public void testPlaceOrders() throws Exception {
    //Give the request id as in the MeshD recorded Golden
    String reqId = "order-1dd936175cbd9b90-e7344dcd-908e-4b91-baca-e2dac40b4cb6";

    //Get the request/response data for the given request id
    JsonNode meshRespObjs = getMeshRequestData(reqId);
    JsonNode requestPayload = meshRespObjs.get(0).get("payload").get(1);
    JsonNode responsePayload = meshRespObjs.get(1).get("payload").get(1);

    String accessToken = obtainAccessToken("admin@admin.com", "pwd");

    //construct the request to the controller using the data fetch from the MeshD stored captured request
    String body = requestPayload.get("body").toString();
    MvcResult mvcResult = mvc.perform(post(requestPayload.get("path").asText())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(body)
        .header("Authorization", "Bearer " + accessToken))
        .andReturn();
    int status = mvcResult.getResponse().getStatus();

    //Write your own assertion rules
    assertEquals(201, status);
  }

  @Test
  public void testGetOrders() throws Exception {
    //Give the request id as in the MeshD recorded Golden
    String reqId = "order-845f8a65ebe1b41a-eef06670-f9c9-487f-bc1d-e7edb29fa9d2";

    //Get the request/response data for the given request id
    JsonNode meshRespObjs = getMeshRequestData(reqId);
    JsonNode requestPayload = meshRespObjs.get(0).get("payload").get(1);
    JsonNode responsePayload = meshRespObjs.get(1).get("payload").get(1);
    String accessToken = obtainAccessToken("admin@admin.com", "pwd");

    //construct the request to the controller using the data fetch from the MeshD stored captured request
    String body = requestPayload.get("body").toString();
    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
        .get(requestPayload.get("path").asText())
        .header("Authorization", "Bearer " + accessToken))
        .andReturn();
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    //write assertion based on the captured response data pulled from MeshD server
    assertEquals(responsePayload.get("body"), mvcResult.getResponse().getContentAsString());
  }
}
