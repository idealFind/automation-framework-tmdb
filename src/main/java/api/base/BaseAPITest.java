package api.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import config.ConfigReader;

public class BaseAPITest {

	protected static RequestSpecification requestSpec;

	@BeforeClass
	public void setup() {

		RestAssured.baseURI = ConfigReader.get("base.url");
		RestAssured.basePath = ConfigReader.get("base.path");

		requestSpec = new RequestSpecBuilder().addHeader("Content-Type", "application/json")
				.addHeader("Authorization", "Bearer " + ConfigReader.get("auth.token")).build();
	}
}
