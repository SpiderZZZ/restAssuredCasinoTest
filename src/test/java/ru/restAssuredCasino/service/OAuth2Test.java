package ru.restAssuredCasino.service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.*;
import ru.restAssuredCasino.service.models.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;


public class OAuth2Test {

    private User user = new User("front_2d6b0a8391742f5d789d7d915755e09e","");
    private static final String oAuth2Path = "/v2/oauth2/token";
    private static final String playersProtectedResourcePath = "/v2/players";
    private String accessToken;

    @BeforeClass
    public void setUp() throws Exception {
        RestAssured.baseURI = "http://test-api.d6.dev.devcaz.com";
        RestAssured.port = 80;
        String jsonBodyForGoodO2Auth = "{\"grant_type\":\"password\",\"username\":\"janedoe_test\",\"password\":\"amFuZWRvZTEyMw==\"}";
        accessToken = given()
                .contentType(ContentType.JSON)
                .body(jsonBodyForGoodO2Auth)
                .auth()
                .preemptive()
                .basic(user.getLogin(),user.getPassword())
                .expect()
                .body(containsString("access_token"))
                .body("access_token", notNullValue())
                .statusCode(200)
                .when()
                .post(oAuth2Path)
                .getBody()
                .jsonPath()
                .getString("access_token");
    }

    @Test
    public void successOAuth2() throws Exception {
        given()
                .contentType(ContentType.JSON)
                .auth()
                .oauth2(accessToken)
                .expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .when()
                .get(playersProtectedResourcePath);
    }

    @Test
    public void errorOAuth2() throws Exception {
        given()
                .contentType(ContentType.JSON)
                .auth()
                .oauth2("2YotnFZFEjr1zCsicMWpAA")
                .expect()
                .statusCode(401)
                .when()
                .get(playersProtectedResourcePath);
    }

    @Test
    public void expiredTokenOAuth2() throws Exception {
        given()
                .contentType(ContentType.JSON)
                .auth()
                .oauth2("eyJ1eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjdhODQ1ZTZiYzc3ZDNkZGI5N2E3NjliMmMxODI0ODJkMTA0YTIwMjlkNTYzOWE5NzAwYmNjNzYxY2Q5OTU0MTA4OTg0ZTVmODQ3NGQzMGFiIn0.eyJhdWQiOiJmcm9udF8yZDZiMGE4MzkxNzQyZjVkNzg5ZDdkOTE1NzU1ZTA5ZSIsImp0aSI6IjdhODQ1ZTZiYzc3ZDNkZGI5N2E3NjliMmMxODI0ODJkMTA0YTIwMjlkNTYzOWE5NzAwYmNjNzYxY2Q5OTU0MTA4OTg0ZTVmODQ3NGQzMGFiIiwiaWF0IjoxNTc1MTE3Nzc3LCJuYmYiOjE1NzUxMTc3NzcsImV4cCI6MTU3NTIwNDE3Nywic3ViIjoiMjQxNyIsInNjb3BlcyI6WyJib251czpyZWFkIiwiZ2FtZTpyZWFkIiwiZ2FtZV9oaXN0b3J5OnJlYWQiLCJqYWNrcG90OnJlYWQiLCJwYXltZW50OnJlYWQiLCJwbGF5ZXI6cmVhZCIsIndpbm5lcjpyZWFkIiwiY2FzaW5vOnJlYWQiLCJtZXNzYWdlOnJlYWQiLCJmYXE6cmVhZCIsImxveWFsdHk6cmVhZCIsImdhbWU6d3JpdGUiLCJwYXltZW50OndyaXRlIiwicGxheWVyOndyaXRlIiwibWVzc2FnZTp3cml0ZSJdfQ.sCAgcXEnf9pujacHL4B8q7_JOUC5CzZgRk500T2-K6HeJVS70mY--qfLarlvBP8a1iPVmg1wRED8U0gdkk8gJ8VAYOA3dzM1haJeOuScmz1AihmTHnB2PgG8JV_ZX8dI1i0vQqQ9Vuu-1MopdfEbOT98uOSRv0oJM-DD8XZ3zkI")
                .expect()
                .statusCode(401)
                .when()
                .get(playersProtectedResourcePath);
    }

}
