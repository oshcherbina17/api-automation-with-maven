package utils;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.User;
import org.apache.commons.lang3.RandomStringUtils;

import static io.restassured.RestAssured.given;

public class UserUtils {

    public static User generateRandomUser() {
        User user = new User();
        user.setName(RandomStringUtils.randomAlphabetic(5));
        user.setEmail(RandomStringUtils.randomAlphabetic(6) + "@gmail.com");
        user.setGender(Math.random() > 0.5 ? "MALE" : "FEMALE");
        user.setStatus("inactive");
        return user;
    }

}

