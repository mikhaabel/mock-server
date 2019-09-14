package com.techtalks.mockserver;

import org.json.JSONException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpError;
import org.mockserver.model.HttpForward;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpForward.forward;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MockServerIT {

    private static ClientAndServer mockServer;
    private static int mockServerPort = 1080;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private static String RESPONSE_BODY = "{ 'id': 137670709, 'name': 'spring-protobuf', 'description': null, 'url': 'https://api.github.com/repos/mikhaabel/spring-protobuf', 'full_name': 'mikhaabel/spring-protobuf', 'private': 'false', 'fork': 'false', 'html_url': 'https://github.com/mikhaabel/spring-protobuf', 'git_url': 'git://github.com/mikhaabel/spring-protobuf.git', 'forks_count': 0, 'stargazers_count': 0, 'watchers_count': 0 }";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @BeforeClass
    public static void startServer() {
        mockServer = startClientAndServer(mockServerPort);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    public void get_list_of_repos_from_mock_server()
            throws JSONException {

        mockServer.reset();

        createExpectationForMockServer();
        ResponseEntity<String> response = createGetRequest();
        JSONAssert.assertEquals(RESPONSE_BODY, response.getBody(), true);
    }


    @Test
    @Ignore // todo: FIX IT!
    public void get_list_of_repos_from_forward()
            throws JSONException {

        mockServer.reset();

        createExpectationForForward();
        ResponseEntity<String> response = createGetRequest();
        JSONAssert.assertEquals(RESPONSE_BODY, response.getBody(), true);
    }

    @Test
    public void get_list_error_from_mock_server() {

        mockServer.reset();

        exceptionRule.expect(ResourceAccessException.class);
        exceptionRule.expectMessage("I/O error on GET request");

        createExpectationWithRandomByteError();
        createGetRequest();
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

    private void createExpectationForMockServer() {
        new MockServerClient("127.0.0.1", mockServerPort)
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

    private void createExpectationForForward() {
        new MockServerClient("127.0.0.1", mockServerPort)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/repos"),
                        exactly(1)
                )
                .forward(
//                        forwardOverriddenRequest(
//                                request()
//                                        .withPath("https://openlibrary.org/api/books?bibkeys=ISBN:1557091528&format=json")
//                                        .withHeader("Content-Type", "application/json; charset=utf-8")
//                        )
                        forward()
                                .withHost("http://www.httpvshttps.com")
                                .withPort(80)
                                .withScheme(HttpForward.Scheme.HTTP)
                );
    }

    private void createExpectationWithRandomByteError() {
        byte[] randomByteArray = new byte[20];
        new Random().nextBytes(randomByteArray);

        new MockServerClient("127.0.0.1", mockServerPort)
                .when(
                        request().withPath("/api/repos")
                )
                .error(
                        HttpError.error()
                                .withDropConnection(true)
                                .withResponseBytes(randomByteArray)
                );
    }


    // example
    private void createExpectationForClosureCallback() {
        new MockServerClient("127.0.0.1", mockServerPort)
                .when(request()
                        .withMethod("GET")
                        .withPath("/api/repos"))
                .forward(
                        httpRequest -> request()
                                .withPath(httpRequest.getPath())
                                .withMethod("POST")
                                .withHeaders(
                                        Header.header("x-callback", "test_callback_header")
                                )
                                .withBody("my_callback")
                );
    }
}
