package utils;

import io.restassured.response.Response;
import static org.testng.Assert.*;

public class APIValidator {

	public static void validateStatusCode(Response response, int expected) {
		assertEquals(response.getStatusCode(), expected);
	}

	public static void validateResponseField(Response response, String jsonPath, String expectedValue) {
		assertEquals(response.jsonPath().getString(jsonPath), expectedValue);
	}
}
