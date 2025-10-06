package interation2;
import interation1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.ProfileInfoSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositUserAccountTest extends BaseTest {
    @Test
    public void userCanMakeDepositIntoOwnAccount() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(id)
                .balance(3000)
                .build();
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK()
        ).post(createDepositRequest);
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(userRequest.getUsername(), userRequest.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, id);
        ModelAssertions.assertThatModels(account, createDepositRequest).match();
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
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(id)
                .balance(sum)
                .build();
        CreateDepositRequest expectedResult = CreateDepositRequest.builder()
                .id(id)
                .balance(0)
                .build();
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsBadRequest(errorMessage)
        ).post(createDepositRequest);
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(userRequest.getUsername(), userRequest.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, id);
        ModelAssertions.assertThatModels(account, expectedResult).match();
    }

    @Test
    public void userCanNotMakeDepositIntoUnCreatedAccount() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(0)
                .balance(100)
                .build();
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsForbidden()
        ).post(createDepositRequest);
    }

    @Test
    public void userCanNotMakeDepositIntoAnotherUsersAccount() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateUserRequest userRequestAnother = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(userRequestAnother.getUsername(), userRequestAnother.getPassword());
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .id(id)
                .balance(100)
                .build();
        CreateDepositRequest expectedResult = CreateDepositRequest.builder()
                .id(id)
                .balance(0)
                .build();
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsForbidden()
        ).post(createDepositRequest);
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(userRequestAnother.getUsername(), userRequestAnother.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, id);
        ModelAssertions.assertThatModels(account, expectedResult).match();
    }
}
