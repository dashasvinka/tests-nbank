package interation2.ui;

import api.models.CreateUserRequest;
import api.models.GetProfileInfoResponse;
import api.models.LoginUserRequest;
import api.models.UpdateNameRequest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import api.generators.RandomModelGenerator;
import api.models.*;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.TestData;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateUserNameTest {

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
    public void userCanUpdateNameWithCorrectData() {
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

        // ШАГ 1 Юзер открывает профиль своего кабинета
        $(Selectors.byText("Noname")).click();

        // ШАГ 2 Юзер вводит новое имя и сохраняет его
        UpdateNameRequest updateNameRequest = RandomModelGenerator.generate(UpdateNameRequest.class);
        $(Selectors.byText("Noname")).click();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(updateNameRequest.getName());
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 3 Проверка, что аккаунт создался на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains( "✅ Name updated successfully!");
        alert.accept();
        Selenide.open("/edit-profile");
        $(Selectors.byText(updateNameRequest.getName())).shouldBe(Condition.visible);


        // ШАГ 4 Проверка, что аккаунт создался на API
        GetProfileInfoResponse getProfileInfoResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);
        ModelAssertions.assertThatModels(updateNameRequest, getProfileInfoResponse).match();
    }

    @Test
    public void userCannotUpdateNameWithInсorrectData() {
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

        // ШАГ 1 Юзер открывает профиль своего кабинета
        $(Selectors.byText("Noname")).click();

        // ШАГ 2 Юзер вводит новое невалидное ограничениям имя и сохраняет его
        UpdateNameRequest updateNameRequest = RandomModelGenerator.generate(UpdateNameRequest.class);
        updateNameRequest.setName("ariana");
        $(Selectors.byText("Noname")).click();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(updateNameRequest.getName());
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 3 Проверка, что аккаунт не создался на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains( "Name must contain two words with letters only");
        alert.accept();
        Selenide.open("/edit-profile");
        $(Selectors.byText("Noname")).shouldBe(Condition.visible);

        // ШАГ 4 Проверка, что аккаунт не создался на API
        UpdateNameRequest expectedResult = TestData.buildUpdateNameRequest(null);
        GetProfileInfoResponse getProfileInfoResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);
        ModelAssertions.assertThatModels(expectedResult, getProfileInfoResponse).match();
    }
}
