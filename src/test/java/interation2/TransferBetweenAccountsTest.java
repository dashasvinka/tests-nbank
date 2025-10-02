package interation2;

import generators.RandomData;
import io.restassured.response.Response;
import models.CreateDepositRequest;
import models.CreateTransferRequest;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.CreateDepositRequester;
import requests.CreateTransferRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferBetweenAccountsTest {

    @Test
    public void userCanMakeTransferBetweenOwnAccount() {

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
    }

    @Test
    public void userCanNotMakeTransferBetweenOwnAccountsOver10000() {
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
    }

    @Test
    public void userCanNotMakeTransferBetweenOwnAccountsEqual0() {
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
                .amount(0)
                .build();

        // создание трансфера в аккауунт 2
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Transfer amount must be at least 0.01"))
                .post(createTransferRequest);
    }

    @Test
    public void userCanNotMakeTransferBetweenOwnAccountsNegativeAmount() {
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
                .amount(-10.01)
                .build();

        // создание трансфера в аккауунт 2
        new CreateTransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Transfer amount must be at least 0.01"))
                .post(createTransferRequest);
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
