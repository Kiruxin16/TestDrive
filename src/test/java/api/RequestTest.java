package api;

import api.pojo.*;
import api.pojo.games.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
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

        FileWriter writer = null;
        try {
            writer = new FileWriter("data.txt");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer,response.getRegisterData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Assert.assertTrue(response.getRegisterData().getId()!=null);
        Assert.assertTrue(response.getRegisterData().getLogin()!=null);
        Assert.assertTrue(response.getRegisterData().getPass()!=null);
        Assert.assertTrue(response.getInfo().getMessage()!=null);


/*        String token = given()
                .body(new TokenRequest(request))
                .when()
                .post("/api/login")
                .then()
                .extract().body().jsonPath().getString("token");

        given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .delete("/api/user");*/
    }



    @Test
    public void gamesAmountTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL));
        RegisterData userData;
        try {
            FileReader fileReader = new FileReader("data.txt");
            ObjectMapper mapper=new ObjectMapper();
             userData= mapper.readValue(fileReader,RegisterData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TokenRequest tokenRequest = new TokenRequest(userData.getPass(),userData.getLogin());
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


    @Test
    public void changePassTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpec(200));
        ObjectMapper mapper=new ObjectMapper();
        RegisterData userData;
        try {
            FileReader fileReader = new FileReader("data.txt");
            userData= mapper.readValue(fileReader,RegisterData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String newPass=null;
        while (newPass==null||newPass.equals(userData.getPass())){
            newPass=Utilities.generateRegData();
        }

        String token = given().log().body()
                .body(new TokenRequest(userData.getPass(),userData.getLogin()))
                .when()
                .post("/api/login")
                .then()
                .extract().body().jsonPath().getString("token");

        given().log().body()
                .body("{\n\"password\": \""+newPass+"\"\n}")
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .put("/api/user")
                .then().log().all();

        try {
            FileWriter fileWriter = new FileWriter("data.txt");
            mapper.writeValue(fileWriter,new RegisterData(userData.getId(),userData.getLogin(),newPass ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        given()
                .body(new TokenRequest(newPass,userData.getLogin()))
                .when()
                .post("/api/login")
                .then().log().status();
    }


    @Test
    public void checkPassFieldChangedTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpec(200));
        ObjectMapper mapper=new ObjectMapper();
        RegisterData userData;
        try {
            FileReader fileReader = new FileReader("data.txt");
            userData= mapper.readValue(fileReader,RegisterData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String token = given().log().body()
                .body(new TokenRequest(userData.getPass(), userData.getLogin()))
                .when()
                .post("/api/login")
                .then()
                .extract().body().jsonPath().getString("token");

        RegisterData userCheckData = given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .get("/api/user")
                .then().log().all()
                .extract().body().as(RegisterData.class);
        Assert.assertEquals(userCheckData.getPass(), userData.getPass());

    }

    @Test
    public void deleteUserTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpec(200));
        ObjectMapper mapper=new ObjectMapper();
        RegisterData userData;
        try {
            FileReader fileReader = new FileReader("data.txt");
            userData= mapper.readValue(fileReader,RegisterData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String token = given().log().body()
                .body(new TokenRequest(userData.getPass(), userData.getLogin()))
                .when()
                .post("/api/login")
                .then()
                .extract().body().jsonPath().getString("token");

        Info info = given()
                .header(new Header("Authorization","Bearer "+token))
                .when()
                .delete("/api/user")
                .then().log().all()
                .extract().body().jsonPath().getObject("info",Info.class);

        Assert.assertEquals(info.getStatus(),"success");
        Assert.assertEquals(info.getMessage(),"User successfully deleted");
    }


    @Test
    public void downloadTest() throws IOException {
        byte[] file= given()
                .contentType("application/octet-stream")
                .when()
                .get(URL+"/api/files/download")
                .then().log().all()
                .statusCode(200)
                .extract().body().asByteArray();

        try {
            FileOutputStream out = new FileOutputStream("test.jpg");
            out.write(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    public void uploadFileTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpec(200));
        given()
                .contentType("multipart/form-data")
                .multiPart(new File("thumb.jpg"))
                .when()
                .post("/api/files/upload")
                .then().log().all();
    }








}
