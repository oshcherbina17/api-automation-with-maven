package tests;

import io.restassured.response.Response;
import utils.GraphQLQuery;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.hamcrest.Matchers;
import net.minidev.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;


public class GraphQLAPITests {
    private static final String BEARER_TOKEN = "Bearer f3175decdafff34fb6ba106098c5543b505a931773d7aff2475d49e229f2b17e";
    public static Integer newUserId;

    public static String email = RandomStringUtils.randomAlphabetic(8) + "@mail.com";

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://gorest.co.in/public/v2";
        RequestSpecification request = given();
        request.header(new Header("Authorization", BEARER_TOKEN));
        request.header(new Header("Content-type", "application/json"));
        GraphQLQuery createUserMutation = new GraphQLQuery();
        createUserMutation.setQuery(
                "mutation ($name: String!, $gender: String!, $email: String!, $status: String!) { createUser( input: { name: $name, gender: $gender, email: $email, status: $status } ) { user { id name gender email status }} }");
        JSONObject createUserVars = new JSONObject();
        createUserVars.put("name", "John Snow");
        createUserVars.put("email", email);
        createUserVars.put("gender", "female");
        createUserVars.put("status", "active");
        createUserMutation.setVariables(createUserVars.toString());
        request.body(createUserMutation);
        newUserId = request.post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .extract()
                .path("data.createUser.user.id");
        System.out.println("The new user id: " + newUserId);
    }

    @AfterClass
    public static void teardown() {
        GraphQLQuery deleteUserMutation = new GraphQLQuery();
        deleteUserMutation.setQuery(
                "mutation ($id: Int!) { deleteUser(input: { id: $id }) { user { id name gender email  status  } } }");
        JSONObject deleteUserVars = new JSONObject();
        deleteUserVars.put("id", newUserId);
        deleteUserMutation.setVariables(deleteUserVars.toString());
        given()
                .headers(
                        "Authorization",
                        BEARER_TOKEN,
                        "Content-Type",
                        ContentType.JSON)
                .body(deleteUserMutation)
                .when()
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.deleteUser.user.id", Matchers.equalTo(newUserId));
    }

    @Test
    public void testGetAllUsers() {
        GraphQLQuery getAllUsersQuery = new GraphQLQuery();
        getAllUsersQuery.setQuery(
                "{ users {nodes { id name email gender status } } }");
        given()
                .header(new Header("Authorization", BEARER_TOKEN))
                .header(new Header("Content-type", "application/json"))
                .body(getAllUsersQuery)
                .when()
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.users.nodes.size()", Matchers.greaterThan(0));
    }

    @Test
    public void testGetUserById() {
        GraphQLQuery getUserDetailsQuery = new GraphQLQuery();
        getUserDetailsQuery.setQuery(
                "query ($id: ID!) { user(id: $id) { id name email gender status } }");
        JSONObject getUserDetailsVars = new JSONObject();
        getUserDetailsVars.put("id", newUserId);
        getUserDetailsQuery.setVariables(getUserDetailsVars.toString());
        given()
                .headers(
                        "Authorization",
                        BEARER_TOKEN,
                        "Content-Type",
                        ContentType.JSON)
                .body(getUserDetailsQuery)
                .when()
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.user.name", Matchers.equalTo("John Snow"))
                .body("data.user.email", Matchers.equalTo(email));
    }

    @Test
    public void testUpdateUserName() {
        String nameToUpdate = "Update name";
        GraphQLQuery updateUserMutation = new GraphQLQuery();
        updateUserMutation.setQuery(
                "mutation ($id: Int!, $name: String!) { updateUser ( input: { id: $id, name: $name }) { user { id name gender email status  } } }");
        JSONObject updateUserVars = new JSONObject();
        updateUserVars.put("name", nameToUpdate);
        updateUserVars.put("id", newUserId);
        updateUserMutation.setVariables(updateUserVars.toString());

        given()
                .headers(
                        "Authorization",
                        BEARER_TOKEN,
                        "Content-Type",
                        ContentType.JSON)
                .body(updateUserMutation)
                .when()
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.updateUser.user.name", Matchers.equalTo(nameToUpdate));
    }

    @Test
    public void testUpdateUserStatus() {
        String newStatus = "inactive";
        GraphQLQuery updateUserStatusMutation = new GraphQLQuery();
        updateUserStatusMutation.setQuery(
                "mutation ($id: Int!, $status: String!) { updateUser(input: { id: $id, status: $status }) { user { id name gender email status } } }");
        JSONObject updateUserStatusVars = new JSONObject();
        updateUserStatusVars.put("id", newUserId);
        updateUserStatusVars.put("status", newStatus);
        updateUserStatusMutation.setVariables(updateUserStatusVars.toString());

        given()
                .headers(
                        "Authorization",
                        BEARER_TOKEN,
                        "Content-Type",
                        ContentType.JSON)
                .body(updateUserStatusMutation)
                .when()
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.updateUser.user.status", Matchers.equalTo(newStatus));
    }

    @Test
    public void testUpdateUserEmail() {
        String newEmail = RandomStringUtils.randomAlphabetic(8) + "@mail.com";
        GraphQLQuery updateUserEmailMutation = new GraphQLQuery();
        updateUserEmailMutation.setQuery(
                "mutation ($id: Int!, $email: String!) { updateUser(input: { id: $id, email: $email }) { user { id name gender email status } } }");
        JSONObject updateUserEmailVars = new JSONObject();
        updateUserEmailVars.put("id", newUserId);
        updateUserEmailVars.put("email", newEmail);
        updateUserEmailMutation.setVariables(updateUserEmailVars.toString());

        given()
                .headers(
                        "Authorization",
                        BEARER_TOKEN,
                        "Content-Type",
                        ContentType.JSON)
                .body(updateUserEmailMutation)
                .when()
                .post("/graphql")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.updateUser.user.email", Matchers.equalTo(newEmail));
    }
}
