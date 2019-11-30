package ru.restAssuredCasino.service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import ru.restAssuredCasino.service.models.Player;
import ru.restAssuredCasino.service.models.User;
import org.testng.annotations.*;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;


public class PlayerTest {

    private User user = new User("front_2d6b0a8391742f5d789d7d915755e09e","");
    private String jsonBodyForGoodBasicAuth = "{\"grant_type\":\"client_credentials\",\"scope\":\"guest:default\"}";
    private static final String oAuth2Path = "/v2/oauth2/token";
    private static final String registerNewPlayerPath = "/v2/players";
    private static final String getSinglePlayerProfile = "/v2/players/%s";
    private String accessToken;
    private String playerResourceAccessToken;
    private Player player = new Player().RandomPlayer();;

    @BeforeClass
    public void setUp() throws Exception {
        RestAssured.baseURI = "http://test-api.d6.dev.devcaz.com";
        RestAssured.port = 80;
        accessToken = given()
                .contentType(ContentType.JSON)
                .body(jsonBodyForGoodBasicAuth)
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
    public void successCreatePlayer() throws Exception {
        //Because in system added only RUB at admins page http://test-app.d6.dev.devcaz.com/structure/nodeCurrency/admin
        String currencyRule = System.getProperty("currencyrule", "RUB");
        if (currencyRule.equals("RUB")) {
            player.setCurrency(Currency.getInstance("RUB"));
        }
        String playerId = given()
                .contentType(ContentType.JSON)
                .body(player.getPlayerJson())
                .auth()
                .oauth2(accessToken)
                .expect()
                .statusCode(201)
                .body(containsString("id"))
                .body("id", notNullValue())
                .body(matchesJsonSchemaInClasspath("response_player_add_success_schema.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(registerNewPlayerPath)
                .getBody()
                .jsonPath()
                .getString("id");
        player.setId(playerId);
    }

    @Test
    public void errorCreatePlayer() throws Exception {
        //Because in system added only RUB at admins page http://test-app.d6.dev.devcaz.com/structure/nodeCurrency/admin
        //If added more currency, need change test
        player.setCurrency(Currency.getInstance("EUR"));
        given()
                .contentType(ContentType.JSON)
                .body(player.getPlayerJson())
                .auth()
                .oauth2(accessToken)
                .expect()
                .statusCode(403)
                .body(matchesJsonSchemaInClasspath("response_player_add_error_schema.json"))
                .contentType(ContentType.JSON)
                .when()
                .post(registerNewPlayerPath);

    }

    @Test(dependsOnMethods = "successCreatePlayer")
    public void getPlayerResourceToken()
    {
        Map<String, Object> jsonPlayerAuth = new HashMap<>();
        jsonPlayerAuth.put("grant_type","password");
        jsonPlayerAuth.put("username",player.getUsername());
        jsonPlayerAuth.put("password",player.getPasswordBase64());

        playerResourceAccessToken = given()
                .contentType(ContentType.JSON)
                .body(jsonPlayerAuth)
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

    @Test(dependsOnMethods = {"successCreatePlayer","getPlayerResourceToken"})
    public void getPlayerProfileInfoSuccess()
    {
        given()
                .contentType(ContentType.JSON)
                .auth()
                .preemptive()
                .oauth2(playerResourceAccessToken)
                .expect()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("response_get_player.json"))
                .when()
                .get(String.format(getSinglePlayerProfile,player.getId()))
                .then();
    }

    @Test(dependsOnMethods = {"successCreatePlayer","getPlayerResourceToken"})
    public void getPlayerProfileInfoError()
    {
        given()
                .contentType(ContentType.JSON)
                .auth()
                .preemptive()
                .oauth2(playerResourceAccessToken)
                .expect()
                .statusCode(404)
                .when()
                .get(String.format(getSinglePlayerProfile, RandomStringUtils.randomNumeric(10)));
    }
}
