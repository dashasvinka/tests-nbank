package interation1.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.baseUrl = " http://localhost:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    public void userCanCreateAccountTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1 Админ логинится в банке
        // ШАГ 2 Админ создает пользователя
        // ШАГ 3 Юзер логинится в банке

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/login");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 4 Юзер создает аккаунт
        $(Selectors.byText("➕ Create New Account")).click();

        // ШАГ 5 Проверка, что аккаунт создался на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains( "✅ New Account Created! Account Number:");

        alert.accept();

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);

        matcher.find();

        String createdAccNumber = matcher.group(1);

        // ШАГ 6 Проверка, что аккаунт создался на API
        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(),user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse createdAccount = Arrays.stream(existingUserAccounts).filter(
                account -> account.getAccountNumber().equals(createdAccNumber)).findFirst().get();

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getBalance()).isZero();
        assertThat(existingUserAccounts).hasSize(1);
    }
}
