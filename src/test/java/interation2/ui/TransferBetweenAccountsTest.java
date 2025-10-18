package interation2.ui;

import api.models.*;
import api.requests.steps.*;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import api.models.*;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import java.util.Map;
import java.util.regex.Pattern;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class TransferBetweenAccountsTest {
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
    public void userCanMakeValidTransferTest() {

        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());

        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,3000);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 1000);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,2000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,1000);


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

        // ШАГ 1 Юзер открывает страницу совершения трансфера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // ШАГ 2 Юзер заполняет форму для совершения трансфера и подтверждает трансфер
        String amount =  Double.toString(createTransferRequest.getAmount());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys((amount));
        String idRecipientAccount =  "ACC" + Long.toString(createTransferRequest.getReceiverAccountId());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys((idRecipientAccount));
        $(Selectors.byClassName("form-group")).find("select").click();
        String idAccount =  Long.toString(createDepositRequest.getId());
        $(Selectors.byValue(idAccount)).click();
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 3 Проверка, что трансфер успешно осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains( "✅ Successfully transferred");
        alert.accept();

        var m = Pattern.compile("(?<=ACC)\\d+").matcher(alertText);
        m.find();
        assert Long.parseLong(m.group()) == idSecond;

        var p = Pattern.compile("(?<=\\$)\\d+(?:\\.\\d+)?").matcher(alertText);
        p.find();
        assertThat(Double.parseDouble(p.group()))
                .isCloseTo(createTransferRequest.getAmount(), within(0.0001));

        // ШАГ 4 Проверка, что трансфер успешно осуществлен на API
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userMustConfirmTransferTest() {

        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());

        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,3000);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 1000);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,3000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);


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

        // ШАГ 1 Юзер открывает страницу совершения трансфера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // ШАГ 2 Юзер заполняет форму для совершения трансфера и не подтверждает трансфер
        String amount =  Double.toString(createTransferRequest.getAmount());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys((amount));
        String idRecipientAccount =  "ACC" + Long.toString(createTransferRequest.getReceiverAccountId());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys((idRecipientAccount));
        $(Selectors.byClassName("form-group")).find("select").click();
        String idAccount =  Long.toString(createDepositRequest.getId());
        $(Selectors.byValue(idAccount)).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 3 Проверка, что необходимо подтверждение трансфера
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains( "❌ Please fill all fields and confirm.");
        alert.accept();

        // ШАГ 4 Проверка, что трансфер не осуществлен на API
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userCannotTransferForInvalidAccountTest() {

        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());

        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,3000);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 1000);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,3000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);


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

        // ШАГ 1 Юзер открывает страницу совершения трансфера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // ШАГ 2 Юзер заполняет форму для совершения трансфера и не подтверждает трансфер
        String amount =  Double.toString(createTransferRequest.getAmount());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys((amount));
        String idRecipientAccount = Long.toString(createTransferRequest.getReceiverAccountId());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys((idRecipientAccount));
        $(Selectors.byClassName("form-group")).find("select").click();
        String idAccount =  Long.toString(createDepositRequest.getId());
        $(Selectors.byValue(idAccount)).click();
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 3 Проверка, что акаунт не обнаружен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains( "❌ No user found with this account number.");
        alert.accept();

        // ШАГ 4 Проверка, что трансфер не осуществлен на API
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userCannotTransferMoreThan10000Test() {

        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());

        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,3000);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 100000);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,3000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);


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

        // ШАГ 1 Юзер открывает страницу совершения трансфера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // ШАГ 2 Юзер заполняет форму для совершения трансфера и не подтверждает трансфер
        String amount =  Double.toString(createTransferRequest.getAmount());
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys((amount));
        String idRecipientAccount = "ACC" + Long.toString(createTransferRequest.getReceiverAccountId());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys((idRecipientAccount));
        $(Selectors.byClassName("form-group")).find("select").click();
        String idAccount =  Long.toString(createDepositRequest.getId());
        $(Selectors.byValue(idAccount)).click();
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 3 Проверка, что трансфер не может превышать 10000 на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains( "❌ Error: Transfer amount cannot exceed 10000");
        alert.accept();

        // ШАГ 4 Проверка, что трансфер не осуществлен на API
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }
}
