package interation2.api;

import api.models.*;
import api.requests.steps.*;
import interation1.api.BaseTest;
import api.models.*;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferBetweenAccountsTest extends BaseTest {

    @Test
    public void userCanMakeTransferBetweenOwnAccount()  {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,3000);
        DepositSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 1000);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,2000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,1000);
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK()
        ).post(createTransferRequest);
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(userRequest.getUsername(), userRequest.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
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
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,4000);
        for (int i = 0; i < 3; i++) {
            DepositSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);
        }
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, amount);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,12000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(errorMessage)
        ).post(createTransferRequest);

        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(userRequest.getUsername(), userRequest.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userCanNotMakeTransferBetweenOwnAccountsOverDepositAmounts() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,4000);
        DepositSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 5000.01);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,4000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts")
        ).post(createTransferRequest);
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(userRequest.getUsername(), userRequest.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userCanNotMakeTransferBetweenSameAccounts() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idFirst, 1000.01);
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,4000);
        DepositSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                // Defect --> actual successes
                ResponseSpecs.requestReturnsBadRequest()
        ).post(createTransferRequest);
    }

    @Test
    public void userCanNotMakeTransferBetweenNonExistentAccounts() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, 0L, 1000.01);
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,4000);
        DepositSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(createTransferRequest);
    }

    @Test
    public void userCanNotMakeTransferBetweenOwnAndSomeoneElseAccount() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateUserRequest userRequestAnother = AdminSteps.createUser();
        Long idSecond = AccountSteps.createAccountAndGetId(userRequestAnother.getUsername(), userRequestAnother.getPassword());
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 1000);
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,4000);
        DepositSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                // Defect --> actual successes
                ResponseSpecs.requestReturnsBadRequest()
        ).post(createTransferRequest);
    }
}
