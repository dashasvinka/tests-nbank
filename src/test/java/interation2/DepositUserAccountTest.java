package interation2;

import generators.RandomData;
import io.restassured.response.Response;
import models.CreateDepositRequest;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.CreateDepositRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class DepositUserAccountTest {
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
    }

    @Test
    public void userCanNotMakeDepositIntoOwnAccountOver5000() {
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
                .balance(5000.01)
                .build();

        // создание депозита
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Deposit amount cannot exceed 5000"))
                .post(createDepositRequest);
    }

    @Test
    public void userCanNotMakeDepositIntoOwnAccountNegativeSum() {
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
                .balance(-1.01)
                .build();

        // создание депозита
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Deposit amount must be at least 0.01"))
                .post(createDepositRequest);
    }

    @Test
    public void userCanNotMakeDepositIntoOwnAccountSumEqualToZero() {
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
                .balance(-1.01)
                .build();

        // создание депозита
        new CreateDepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Deposit amount must be at least 0.01"))
                .post(createDepositRequest);
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

    }
}
