package interation2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomModelGenerator;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.ProfileInfoSteps;
import requests.steps.TestData;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class DepositUserAccountTest {

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
    public void userCanMakeValidDeposit() {
        CreateUserRequest user = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(id);
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

        // ШАГ 1 Юзер открывает страницу совершения депозита
        $(Selectors.byText("💰 Deposit Money")).click();

        // ШАГ 2 Юзер заполняет форму для совершения депозита и подтверждает депозит
        $(Selectors.byClassName("form-group")).find("select").click();
        String idForDeposit =  Long.toString(createDepositRequest.getId());
        $(Selectors.byValue(idForDeposit)).click();
        String balance =  Double.toString(createDepositRequest.getBalance());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys((balance));
        $(Selectors.byText("💵 Deposit")).click();

        // ШАГ 3 Проверка, что депозит успешно осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains( "✅ Successfully deposited");
        alert.accept();

        var m = Pattern.compile("(?<=ACC)\\d+").matcher(alertText);
        m.find();
        assert Long.parseLong(m.group()) == id;

        var p = Pattern.compile("(?<=\\$)\\d+(?:\\.\\d+)?").matcher(alertText);
        p.find();
        assertThat(Double.parseDouble(p.group()))
                .isCloseTo(createDepositRequest.getBalance(), within(0.0001));

        // ШАГ 3 Проверка, что депозит успешно осуществлен на API
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, id);
        ModelAssertions.assertThatModels(account, createDepositRequest).match();
    }

    @Test
    public void userCannotMakeInvalidDeposit() {
        CreateUserRequest user = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(id);
        createDepositRequest.setBalance(40000);
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

        // ШАГ 1 Юзер открывает страницу совершения депозита
        $(Selectors.byText("💰 Deposit Money")).click();

        // ШАГ 2 Юзер заполняет форму для совершения депозита и подтверждает депозит
        $(Selectors.byClassName("form-group")).find("select").click();
        String idForDeposit =  Long.toString(createDepositRequest.getId());
        $(Selectors.byValue(idForDeposit)).click();
        String balance =  Double.toString(createDepositRequest.getBalance());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys((balance));
        $(Selectors.byText("💵 Deposit")).click();

        // ШАГ 3 Проверка, что депозит не осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please deposit less or equal to 5000$.");
        alert.accept();

        // ШАГ 3 Проверка, что депозит не осуществлен на API

        CreateDepositRequest expectedResult = TestData.buildCreateDepositRequest(id, 0);
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, id);
        ModelAssertions.assertThatModels(account, expectedResult).match();
    }
}
