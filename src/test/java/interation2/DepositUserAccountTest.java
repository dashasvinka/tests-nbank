package interation2;

import generators.RandomData;
import interation1.BaseTest;
import io.restassured.response.Response;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.CreateDepositRequester;
import requests.GetProfileInfoRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositUserAccountTest extends BaseTest {
    @Test
    public void userCanMakeDepositIntoOwnAccount() {
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

        // создание аккаунта
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long id = Long.parseLong(response.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(id)
                .balance(3000)
                .build();

        // создание депозита
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(createDepositRequest);

        // проверяем что баланс изменился и равен пополнению
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel account = result.getAccounts().stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + id + " not found"));

        softly.assertThat(account.getBalance()).isEqualTo(3000);
    }

    public static Stream<Arguments> sumInvalidData() {
        return Stream.of(
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000"),
                Arguments.of(0.00, "Deposit amount must be at least 0.01"),
                Arguments.of(-10.01, "Deposit amount must be at least 0.01"));
    }

    @MethodSource("sumInvalidData")
    @ParameterizedTest
    public void userCanNotMakeDepositIntoOwnAccountInvalidSum(Double sum, String errorMessage) {
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

        // создание аккаунта
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long id = Long.parseLong(response.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(id)
                .balance(sum)
                .build();

        // создание депозита
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .post(createDepositRequest);

        // проверяем что баланс не изменился и равен 0
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel account = result.getAccounts().stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + id + " not found"));

        softly.assertThat(account.getBalance()).isEqualTo(0);
    }

    @Test
    public void userCanNotMakeDepositIntoUnCreatedAccount() {
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

        // создание аккаунта
        Response response = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().response();
        Long id = Long.parseLong(response.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(0)
                .balance(100)
                .build();

        // создание депозита
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(createDepositRequest);
    }

    @Test
    public void userCanNotMakeDepositIntoAnotherUsersAccount() {

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
        new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);

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
        Long id = Long.parseLong(response.jsonPath().getString("id"));

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(id)
                .balance(100)
                .build();

        // создание депозита в чужой аккаунт
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden())
                .post(createDepositRequest);

        // проверяем что баланс не изменился и равен 0
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequestAnother.getUsername(), userRequestAnother.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);

        AccountModel account = result.getAccounts().stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + id + " not found"));

        softly.assertThat(account.getBalance()).isEqualTo(0);
    }
}
