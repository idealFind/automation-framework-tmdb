package api.clients;

import io.restassured.response.Response;
import api.base.BaseAPITest;
import api.models.User;

import static io.restassured.RestAssured.given;

public class UserAPIClient extends BaseAPITest {

	public Response createUser(User user) {
		return given().spec(requestSpec).body(user).when().post("/users");
	}

	public Response getUser(int userId) {
		return given().spec(requestSpec).when().get("/users/" + userId);
	}

	public Response updateUser(int userId, User user) {
		return given().spec(requestSpec).body(user).when().put("/users/" + userId);
	}

	public Response deleteUser(int userId) {
		return given().spec(requestSpec).when().delete("/users/" + userId);
	}
}
