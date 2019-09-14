### Mock Server for system mocking and integration testing

During this talk I will show how to set up, customize and run MockServer with Spring Boot application's REST API based on HTTP/HTTPS. There will be four steps:

1. Generate Spring boot application and setup Maven dependencies
2. Develop controller and http-client for simple query to github open-API
3. Create e2e test for app's REST endpoint, using real data from github
4. Create e2e/system tests, mocking github responses with MockServer

Back-end will be REST API, built on Spring. MockServer framework is used with respective Maven's libraries for its client-server communication, based on Netty.

Thanks Baeldung for the base [material](https://www.baeldung.com/mockserver). Language: Java.


### Additional material:

- Spring Boot: https://spring.io/guides/gs/spring-boot/
- MockServer: http://www.mock-server.com/