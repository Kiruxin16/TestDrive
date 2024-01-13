package api;

import api.pojo.Info;
import api.pojo.games.Game;
import io.restassured.http.Header;
import org.junit.Assert;
import org.junit.Test;
import api.pojo.RegResponse;
import api.pojo.RegRequest;
import api.pojo.TokenRequest;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;

public class RequestTest {

    private static final String URL = "http://85.192.34.140:8080";
    private String regData ;


    @Test
    public void response200Test(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpec(200));
        given()
                .when()
                .get("/api/users")
                .then()
                .log().all();
    }

    @Test
    public void responseErrorTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpec(400));
        given()
                .when()
                .get("/api/bad-request")
                .then()
                .log().all();
    }


    @Test
    public void fieldCheckTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL));
        List<String> existingNames=get("/api/users")
                .then()
                .extract().body().jsonPath().getList("");
        boolean flag = true;
        while (flag){
            regData = Utilities.generateRegData();
            flag=existingNames.stream().anyMatch(n->n.equals(regData));
        }

        RegRequest request = new RegRequest(regData,regData);
        RegResponse response =given().log().body()
                .body(request)
                .when()
                .post("/api/signup")
                .then()
                .log().all()
                .statusCode(201)
                .extract().as(RegResponse.class);


        Assert.assertTrue(response.getRegisterData().getId()!=null);
        Assert.assertTrue(response.getRegisterData().getLogin()!=null);
        Assert.assertTrue(response.getRegisterData().getPass()!=null);
        Assert.assertTrue(response.getInfo().getMessage()!=null);


        String token = given()
                .body(new TokenRequest(request))
                .when()
                .post("/api/login")
                .then()
                .extract().body().jsonPath().getString("token");

        given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .delete("/api/user");
    }



    @Test
    public void gamesAmountTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL));
        TokenRequest tokenRequest = new TokenRequest("string","string");
        String token = given().log().body()
                .body(tokenRequest)
                .when()
                .post("/api/login")
                .then()
                .extract().body().jsonPath().getString("token");

        Integer amountOfGames = given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .get("/api/user/games")
                .then()
                .extract().body().jsonPath().getList("").size();

        Game testGame = Game.builder()
                .company("Larian")
                .description("games award")
                .dlcs(new ArrayList<>())
                .isFree(false)
                .price(3000l)
                .title("Baldurs gate 3")
                .build();

        given().log().body()
                .header(new Header("Authorization","Bearer "+token))
                .body(testGame)
                .when()
                .post("/api/user/games")
                .then().log().all();


        Integer nextAmountOfGames = given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .get("/api/user/games")
                .then()
                .extract().body().jsonPath().getList("").size();

        Assert.assertTrue((nextAmountOfGames.equals(amountOfGames+1)));
    }

    @Test
    public void gameDeleteTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL));
        TokenRequest tokenRequest = new TokenRequest("string","string");
        String token = given().log().body()
                .body(tokenRequest)
                .when()
                .post("/api/login")
                .then()
                .extract().body().jsonPath().getString("token");

        List<Game> games = given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .get("/api/user/games")
                .then().log().all()
                .extract().body().jsonPath().getList("", Game.class);
        Long deleteId = games.get(games.size()-1).getGameId();

        Info info = given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .delete("/api/user/games/"+deleteId)
                .then().log().all()
                .extract().body().jsonPath().getObject("info",Info.class);


        Assert.assertTrue(info.getMessage()!=null);
        Assert.assertTrue(info.getId()==null);

    }



}
