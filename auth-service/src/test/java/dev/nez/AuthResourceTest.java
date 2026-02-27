package dev.nez;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AuthResourceTest {

    @Test
    public void testSuccessfulLogin() {
        String hardwareId = "hw-login-success";
        String password = "mySecretPassword";

        String registerBody = String.format("""
                {
                    "hardwareId": "%s",
                    "password": "%s",
                    "topic": "devices/%s"
                }
                """, hardwareId, password, hardwareId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post("/api/device/auth/register")
                .then()
                .statusCode(200);

        String loginBody = String.format("""
                {
                    "hardwareId": "%s",
                    "password": "%s"
                }
                """, hardwareId, password);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/device/auth/login")
                .then()
                .statusCode(200) // 200 OK
                .body("token", notNullValue());
    }

    @Test
    public void testLoginInvalidPassword() {
        String hardwareId = "hw-login-invalid-pass";
        String password = "mySecretPassword";

        String registerBody = String.format("""
                {
                    "hardwareId": "%s",
                    "password": "%s",
                    "topic": "devices/%s"
                }
                """, hardwareId, password, hardwareId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post("/api/device/auth/register");

        String loginBody = String.format("""
                {
                    "hardwareId": "%s",
                    "password": "wrongPassword"
                }
                """, hardwareId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/device/auth/login")
                .then()
                .statusCode(401); // 401 Unauthorized
    }

    @Test
    public void testLoginDeviceNotFound() {
        String loginBody = """
                {
                    "hardwareId": "hw-not-found",
                    "password": "somePassword"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/api/device/auth/login")
                .then()
                .statusCode(401); // 401 Unauthorized
    }

    @Test
    public void testDuplicateRegistration() {
        String requestBody = """
                {
                    "hardwareId": "hw-duplicate",
                    "password": "mySecretPassword",
                    "topic": "devices/hw-duplicate"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/device/auth/register")
                .then()
                .statusCode(200);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/device/auth/register")
                .then()
                .statusCode(409);
    }
}
