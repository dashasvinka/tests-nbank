package interation2;

import generators.RandomData;
import interation1.BaseTest;
import io.restassured.response.Response;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferBetweenAccountsTest extends BaseTest {

    @Test
    public void userCanMakeTransferBetweenOwnAccount()  {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание аккаунта 1
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idFirst = Long.parseLong(response.jsonPath().getString("id"));

        // создание аккаунта 2
        Response responseOther = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idSecond = Long.parseLong(responseOther.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(idFirst)
                .balance(3000)
                .build();

        // создание депозита в аккауунт 1
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder()
                .senderAccountId(idFirst)
                .receiverAccountId(idSecond)
                .amount(1000)
                .build();

        // создание трансфера в аккауунт 2
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(createTransferRequest);

        // проверяем что баланс получателя изменился и равен переводу
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel account = result.getAccounts().stream()
                .filter(acc -> acc.getId().equals(idSecond))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + idSecond + " not found"));

        softly.assertThat(account.getBalance()).isEqualTo(1000);

        // проверяем что баланс отправителя изменился и равен разнице
        GetProfileInfoResponse resultSender = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel accountSender = resultSender.getAccounts().stream()
                .filter(acc -> acc.getId().equals(idFirst))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + idFirst + " not found"));

        softly.assertThat(accountSender.getBalance()).isEqualTo(2000);
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of("10000.01","Transfer amount cannot exceed 10000"),
                Arguments.of("-100.10", "Transfer amount must be at least 0.01"),
                Arguments.of("0", "Transfer amount must be at least 0.01"));
    }
    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotMakeTransferBetweenOwnAccountsWithInvalidAmount(Double amount, String errorMessage) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание аккаунта 1
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idFirst = Long.parseLong(response.jsonPath().getString("id"));

        // создание аккаунта 2
        Response responseOther = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idSecond = Long.parseLong(responseOther.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(idFirst)
                .balance(4000)
                .build();

        // создание последовательно 3 депозитов по 4000 в аккауунт 1

        for (int i = 0; i < 3; i++) {
            new CreateDepositRequester(
                    RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK()
            ).post(createDepositRequest);
        }

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder()
                .senderAccountId(idFirst)
                .receiverAccountId(idSecond)
                .amount(10000.01)
                .build();

        // создание трансфера в аккауунт 2
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Transfer amount cannot exceed 10000"))
                .post(createTransferRequest);

        // проверяем что баланс получателя не изменился и равен 0
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel account = result.getAccounts().stream()
                .filter(acc -> acc.getId().equals(idSecond))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + idSecond + " not found"));

        softly.assertThat(account.getBalance()).isEqualTo(0);

        // проверяем что баланс отправителя не изменился и равен депозиту
        GetProfileInfoResponse resultSender = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel accountSender = resultSender.getAccounts().stream()
                .filter(acc -> acc.getId().equals(idFirst))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + idFirst + " not found"));

        softly.assertThat(accountSender.getBalance()).isEqualTo(12000);
    }

    @Test
    public void userCanNotMakeTransferBetweenOwnAccountsOverDepositAmounts() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание аккаунта 1
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idFirst = Long.parseLong(response.jsonPath().getString("id"));

        // создание аккаунта 2
        Response responseOther = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idSecond = Long.parseLong(responseOther.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(idFirst)
                .balance(4000)
                .build();

        // создание депозита
            new CreateDepositRequester(
                    RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                    ResponseSpecs.requestReturnsOK()
            ).post(createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder()
                .senderAccountId(idFirst)
                .receiverAccountId(idSecond)
                .amount(5000.01)
                .build();

        // создание трансфера в аккауунт 2
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts"))
                .post(createTransferRequest);

        // проверяем что баланс получателя не изменился и равен 0
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel account = result.getAccounts().stream()
                .filter(acc -> acc.getId().equals(idSecond))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + idSecond + " not found"));

        softly.assertThat(account.getBalance()).isEqualTo(0);

        // проверяем что баланс отправителя не изменился и равен депозиту
        GetProfileInfoResponse resultSender = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel accountSender = resultSender.getAccounts().stream()
                .filter(acc -> acc.getId().equals(idFirst))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + idFirst + " not found"));

        softly.assertThat(accountSender.getBalance()).isEqualTo(4000);
    }

    @Test
    public void userCanNotMakeTransferBetweenSameAccounts() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание аккаунта 1
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idFirst = Long.parseLong(response.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(idFirst)
                .balance(4000)
                .build();

        // создание депозита
        new CreateDepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK()
        ).post(createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder()
                .senderAccountId(idFirst)
                .receiverAccountId(idFirst)
                .amount(1001)
                .build();

        // создание трансфера в идентичный аккауунт
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                // Defect --> actual successes
                ResponseSpecs.requestReturnsBadRequest())
                .post(createTransferRequest);
    }

    @Test
    public void userCanNotMakeTransferBetweenNonExistentAccounts() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание аккаунта 1
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idFirst = Long.parseLong(response.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(idFirst)
                .balance(4000)
                .build();

        // создание депозита
        new CreateDepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK()
        ).post(createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder()
                .senderAccountId(idFirst)
                .receiverAccountId(0)
                .amount(1001)
                .build();

        // создание трансфера в идентичный аккауунт
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(createTransferRequest);
    }

    @Test
    public void userCanNotMakeTransferBetweenOwnAndSomeoneElseAccount() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя 1
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание аккаунта 1
        Response responseFirst = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idFirst = Long.parseLong(responseFirst.jsonPath().getString("id"));

        CreateUserRequest userRequestAnother = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя 2
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequestAnother);

        // создание аккаунта 2
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequestAnother.getUsername(), userRequestAnother.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long idSecond = Long.parseLong(response.jsonPath().getString("id"));

        // создание депозита в аккаунт 1
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(idFirst)
                .balance(4000)
                .build();

        // создание депозита
        new CreateDepositRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK()
        ).post(createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder()
                .senderAccountId(idFirst)
                .receiverAccountId(idSecond)
                .amount(1000)
                .build();

        // создание трансфера в чужой аккауунт
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                // Defect --> actual successes
                ResponseSpecs.requestReturnsBadRequest())
                .post(createTransferRequest);
    }
}
