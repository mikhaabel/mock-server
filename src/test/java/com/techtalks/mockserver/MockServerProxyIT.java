package com.techtalks.mockserver;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Format;
import org.mockserver.model.Header;
import org.mockserver.verify.VerificationTimes;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MockServerProxyIT {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, true, mockServerPort);

    private MockServerClient mockServerClient;

    private static int mockServerPort = 1080;

    private static String RESPONSE_BODY = "{ 'id': 137670709, 'name': 'spring-protobuf', 'description': null, 'url': 'https://api.github.com/repos/mikhaabel/spring-protobuf', 'full_name': 'mikhaabel/spring-protobuf', 'private': 'false', 'fork': 'false', 'html_url': 'https://github.com/mikhaabel/spring-protobuf', 'git_url': 'git://github.com/mikhaabel/spring-protobuf.git', 'forks_count': 0, 'stargazers_count': 0, 'watchers_count': 0 }";

    @Test
    public void get_list_of_repos_from_mock_server()
            throws JSONException {

        mockServerClient.reset();

        createResponseExpectation();

        ResponseEntity<String> response = createGetRequest();

        verifyRequests();

        log.info("LOGS of exp and req: {}", retrieveLogsExpectationsAndRequests());

        log.info("All req: {}", retrieveRequestsAsJson());

        JSONAssert.assertEquals(RESPONSE_BODY, response.getBody(), true);
    }


    private ResponseEntity<String> createGetRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.github.v3+json");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(
                "http://localhost:" + mockServerPort + "/api/repos",
                HttpMethod.GET,
                entity,
                String.class);
    }

    private String retrieveLogsExpectationsAndRequests() {
        return mockServerClient
                .retrieveLogMessages(request());
    }

    private String retrieveRequestsAsJson() {
        return mockServerClient
                .retrieveRecordedRequests(
                        request()
                                .withPath("/api/repos")
                                .withMethod("GET"),
                        Format.JSON
                );
    }

    private void verifyRequests() {
        mockServerClient
                .verify(
                        request()
                                .withPath("/api/repos"),
                        VerificationTimes.once()
                );
    }

    private void createResponseExpectation() {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/repos")
                                .withHeader("Content-type", "application/vnd.github.v3+json"),
                        exactly(1))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Transfer-Encoding", "chunked"))
                                .withBody(RESPONSE_BODY)
                                .withDelay(TimeUnit.SECONDS, 1)
                );
    }
}