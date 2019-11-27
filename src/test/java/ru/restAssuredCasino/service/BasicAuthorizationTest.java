package ru.restAssuredCasino.service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.*;
import ru.restAssuredCasino.service.models.User;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;


public class BasicAuthorizationTest {

    private User user = new User("front_2d6b0a8391742f5d789d7d915755e09e","");
    private String jsonBodyForGoodBasicAuth = "{\"grant_type\":\"client_credentials\",\"scope\":\"guest:default\"}";
    private static final String oAuth2Path = "/v2/oauth2/token";

    @BeforeClass
    public void setUp() throws Exception {
        RestAssured.baseURI = "http://test-api.d6.dev.devcaz.com";
        RestAssured.port = 80;
    }
    @Test
    public void successBasicAuth() throws Exception {
    given()
            .contentType(ContentType.JSON)
            .body(jsonBodyForGoodBasicAuth)
            .auth()
            .preemptive()
            .basic(user.getLogin(), user.getPassword())
            .expect()
            .statusCode(200)
            .when()
            .post(oAuth2Path)
    ;
    }

    @Test
    public void errorAuthorizationRequiredBasicAuth() throws Exception {
        given()
                .contentType(ContentType.JSON)
                .body(jsonBodyForGoodBasicAuth)
                .auth()
                .preemptive()
                .basic("test", "test")
                .expect()
                .statusCode(401)
                .when()
                .post(oAuth2Path );
    }

    @Test
    public void errorAuthorizationBadBodyBasicAuth() throws Exception {
        String jsonBodyForBadBasicAuth = "{\"grant_type\":\"client_credentials\",\"scope\":\"test\"}";
        given()
                .contentType(ContentType.JSON)
                .body(jsonBodyForBadBasicAuth)
                .auth()
                .preemptive()
                .basic("test", "test")
                .expect()
                .statusCode(401)
                .when()
                .post(oAuth2Path );
    }

    @Test
    public void badContentTypeBasicAuth() throws Exception {
        given()
                .contentType(ContentType.XML)
                .body(jsonBodyForGoodBasicAuth)
                .auth()
                .preemptive()
                .basic(user.getLogin(), user.getPassword())
                .expect()
                .statusCode(400)
                .when()
                .post(oAuth2Path)
        ;
    }

    @Test
    public void tokenInAnswerBasicAuth() throws Exception {
        given()
                .contentType(ContentType.JSON)
                .body(jsonBodyForGoodBasicAuth)
                .auth()
                .preemptive()
                .basic(user.getLogin(), user.getPassword())
                .expect()
                .body(containsString("access_token"))
                .body("access_token", notNullValue())
                .when()
                .post(oAuth2Path )
        ;
    }

    @Test
    public void successO2Auth()
    {
        String jsonBodyForGoodO2Auth = "{\"grant_type\":\"password\",\"username\":\"janedoe_test\",\"password\":\"amFuZWRvZTEyMw==\"}";
        String accessToken = given()
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

        given()
                .contentType(ContentType.JSON)
                .auth()
                .oauth2(accessToken)
                .expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .when()
                .get("/v2/players");
    }

    @Test
    public void errorGetAccessToken()
    {
        String jsonBodyForBadO2Auth = "{\"grant_type\":\"password\",\"username\":\"janedoe_test\",\"password\":\"am9obmRvZTEyMw==\"}";
        given()
                .contentType(ContentType.JSON)
                .body(jsonBodyForBadO2Auth)
                .auth()
                .preemptive()
                .basic(user.getLogin(),user.getPassword())
                .expect()
                .body(containsString("Unauthorized"))
                .body("message", notNullValue())
                .statusCode(401)
                .when()
                .post(oAuth2Path);
    }
}
