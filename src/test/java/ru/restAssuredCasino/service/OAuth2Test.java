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
    public void forbiddenOAuth2() throws Exception {
        given()
                .contentType(ContentType.JSON)
                .auth()
                .oauth2("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjJkMmUyZTc3NDJjYTgxNjkxNDg4N2IwNjc1NjY1MTgxYTk4M2ZmMTkxZGJiZmFlZDk5MjEzM2MwZjI1ZDAyMTQ5OTZjZGRiOGI5ZGQ5YWU4In0.eyJhdWQiOiJmcm9udF8yZDZiMGE4MzkxNzQyZjVkNzg5ZDdkOTE1NzU1ZTA5ZSIsImp0aSI6IjJkMmUyZTc3NDJjYTgxNjkxNDg4N2IwNjc1NjY1MTgxYTk4M2ZmMTkxZGJiZmFlZDk5MjEzM2MwZjI1ZDAyMTQ5OTZjZGRiOGI5ZGQ5YWU4IiwiaWF0IjoxNTc0Nzc5MDI3LCJuYmYiOjE1NzQ3NzkwMjcsImV4cCI6MTU3NDg2NTQyNywic3ViIjoiIiwic2NvcGVzIjpbImd1ZXN0OmRlZmF1bHQiXX0.vK62dwRT-q8svIi8xjQIf8O17OjCt3xTbftL1JyEtdV8BtZI2ehRDIXsfY_YCfAucIcUq8HekJuIKFbn-adI5Ae_vWDxUV6cH_DLfI6CCVoZUbKQ8sVOD2iXORQQvA_xwaG-iuoQkpz-dRl9zVa72JXdbSiam75o0SLa4pZS3-s")
                .expect()
                .statusCode(403)
                .when()
                .get("/v2/players");
    }

}
