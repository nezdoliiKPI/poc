package dev.nez;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AuthResourceTest extends AuthTestBase {

    @Test
    public void testSuccessfulLogin() {
        final String hardwareId = "hw-login-success";
        final String password = "mySecretPassword";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(getRegisterBody(hardwareId, password))
                .when()
                .post("/api/device/auth/register")
                .then()
                .statusCode(201);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(getLoginBody(hardwareId, password))
                .when()
                .post("/api/device/auth/login")
                .then()
                .statusCode(200) // 200 OK
                .body("token", notNullValue());
    }

    @Test
    public void testLoginInvalidPassword() {
        final String hardwareId = "hw-login-invalid-pass";
        final String password = "mySecretPassword";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(getRegisterBody(hardwareId, password))
                .when()
                .post("/api/device/auth/register");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(getLoginBody(hardwareId, "invalidPassword"))
                .when()
                .post("/api/device/auth/login")
                .then()
                .statusCode(401); // 401 Unauthorized
    }

    @Test
    public void testLoginDeviceNotFound() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(getLoginBody("hw-not-found", "somePassword"))
                .when()
                .post("/api/device/auth/login")
                .then()
                .statusCode(401); // 401 Unauthorized
    }

    @Test
    public void testDuplicateRegistration() {
        final String hardwareId = "hw-duplicate";
        final String password = "mySecretPassword";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(getRegisterBody(hardwareId, password))
                .when()
                .post("/api/device/auth/register")
                .then()
                .statusCode(201);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(getRegisterBody(hardwareId, password))
                .when()
                .post("/api/device/auth/register")
                .then()
                .statusCode(409);
    }
}

abstract class AuthTestBase {
    protected String getRegisterBody(String hardwareId, String password) {
        final String topic = "data";
        final String batteryTopic = "battery/data";
        final String messageType = "JSON";

        return String.format("""
                {
                    "hardwareId": "%s",
                    "password": "%s",
                    "topic": "devices/%s",
                    "batteryTopic": "devices/%s",
                    "messageType": "%s"
                }
                """, hardwareId, password, topic, batteryTopic, messageType);
    }

    protected String getLoginBody(String hardwareId, String password) {
        return  String.format("""
                {
                    "hardwareId": "%s",
                    "password": "%s"
                }
                """, hardwareId, password);
    }
}
