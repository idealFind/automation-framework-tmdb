package tests.api;

import api.base.BaseAPITest;
import api.clients.UserAPIClient;
import io.restassured.response.Response;
import api.models.User;

import org.testng.annotations.Test;
import utils.APIValidator;

import java.util.UUID;

public class UserAPITest extends BaseAPITest {

	int userId;
	UserAPIClient client = new UserAPIClient();

	String randomEmail = "user" + UUID.randomUUID() + "@mail.com";

	@Test(priority = 1)
	public void createUserTest() {

		User user = new User("John Doe", randomEmail, "male", "active");

		Response response = client.createUser(user);

		APIValidator.validateStatusCode(response, 201);

		userId = response.jsonPath().getInt("id");
		System.out.println("Created User ID: " + userId);
	}

	@Test(priority = 2, dependsOnMethods = "createUserTest")
	public void getUserTest() {

		Response response = client.getUser(userId);

		APIValidator.validateStatusCode(response, 200);
	}

	@Test(priority = 3, dependsOnMethods = "createUserTest")
	public void updateUserTest() {

		User updatedUser = new User("John Updated", randomEmail, "female", "active");

		Response response = client.updateUser(userId, updatedUser);

		APIValidator.validateStatusCode(response, 200);
	}

	@Test(priority = 4, dependsOnMethods = "updateUserTest")
	public void deleteUserTest() {

		Response response = client.deleteUser(userId);

		APIValidator.validateStatusCode(response, 204);
	}
}
