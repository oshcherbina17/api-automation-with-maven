package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import services.ApiService;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static utils.UserUtils.generateRandomUser;

public class UserTest {

   private final String BEARER_TOKEN = "Bearer f3175decdafff34fb6ba106098c5543b505a931773d7aff2475d49e229f2b17e";

    private final List<Long> userIds = new ArrayList<>();

    private final ApiService apiService = new ApiService(BEARER_TOKEN);

    @BeforeTest
    public static void init() {
        RestAssured.baseURI = "https://gorest.co.in";
        RestAssured.basePath = "/public/v2";
    }

    @AfterMethod(groups = "created", alwaysRun = true)
    public void deleteCreatedUser() {
        userIds.forEach(apiService::deleteUser);
        userIds.clear();
    }

    @Test
    public void getAllUsersTest() {
        Response response = given()
                .when()
                .get("/users");
        response.prettyPrint();
        response.then().statusCode(200);

        List<User> userList = response.jsonPath().getList(".", User.class);
        Assert.assertFalse(userList.isEmpty(), "User list shouldn`t be empty!");
    }

    @Test(groups = "created")
    public void getUserByIdTest() {
        User createdUser = generateRandomUser();
        createdUser = apiService.createUser(createdUser);
        User getUser = apiService.getUser(createdUser.getId());
        Assert.assertEquals(getUser, createdUser, "User is not as excepted!");
    }


    @Test(groups = "created")
    public void createUserTest() {
        User user = generateRandomUser();
        User createdUser = apiService.createUser(user);
        userIds.add(createdUser.getId());
        User getUser = apiService.getUser(createdUser.getId());
        Assert.assertEquals(getUser, createdUser, "User is not created!");
    }

    @Test(groups = "created")
    public void updateUserTest() {
        User user = generateRandomUser();
        user = apiService.createUser(user);
        userIds.add(user.getId());

        String initialUserName = user.getName();

        User updatedUser = user;
        updatedUser.setName("UpdatedName");

       int statusCode = apiService.updateUser(updatedUser);
        User getUser = apiService.getUser(updatedUser.getId());

        Assert.assertEquals(statusCode, 200);
        Assert.assertNotEquals(getUser.getName(), initialUserName, "User name should not be equal with created user name!");
        Assert.assertEquals(getUser, updatedUser, "User is not updated!");
    }

    @Test
    public void deleteUserTest() {
        User createdUser = generateRandomUser();
        createdUser = apiService.createUser(createdUser);
        apiService.deleteUser(createdUser.getId());

        Response getResponse = given()
                .header("Authorization", BEARER_TOKEN)
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
                .when()
                .get("/users/" + createdUser.getId());

        getResponse.prettyPrint();
        getResponse.then().statusCode(404);
    }

    @Test
    public void getUserWithInvalidIdTest() {
        Response getResponse = given()
                .header("Authorization", BEARER_TOKEN)
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
                .when()
                .get("/users/" + RandomStringUtils.randomNumeric(100));

        getResponse.prettyPrint();
        getResponse.then().statusCode(404);
    }

    @Test
    public void updateUserWithInvalidIdTest() {
        User user = generateRandomUser();
        user = apiService.createUser(user);
        userIds.add(user.getId());

        User updatedUser = user;
        updatedUser.setName("UpdatedName");

        Response putResponse = given()
                .header("Authorization", BEARER_TOKEN)
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
                .when()
                .body(updatedUser)
                .put("/users/" + RandomStringUtils.randomNumeric(100));

        putResponse.prettyPrint();
        putResponse.then().statusCode(404);
    }

    @Test
    public void deleteUserWithInvalidIdTest() {
        Response getResponse = given()
                .header("Authorization", BEARER_TOKEN)
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
                .when()
                .delete("/users/" + RandomStringUtils.randomNumeric(100));

        getResponse.prettyPrint();
        getResponse.then().statusCode(404);
    }


    @Test
    public void userCreationWithIncompleteDetailsTest() {
        User user = new User();
        Response response = given()
                .header("Authorization", BEARER_TOKEN)
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
                .when()
                .body(user)
                .post("/users");

        response.prettyPrint();
        response.then().statusCode(422);
    }

    @Test
    public void userCreationWithInvalidEmailTest() {
        User user = new User();
        user.setName(RandomStringUtils.randomAlphabetic(5));
        user.setEmail("543245");
        user.setGender("female");
        user.setStatus("inactive");

        Response response = given()
                .header("Authorization", BEARER_TOKEN)
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
                .when()
                .body(user)
                .post("/users");

        response.prettyPrint();
        response.then().statusCode(422);
    }
}
