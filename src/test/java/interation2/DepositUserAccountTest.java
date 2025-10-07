package interation2;

import generators.RandomModelGenerator;
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
import requests.steps.TestData;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositUserAccountTest extends BaseTest {
    @Test
    public void userCanMakeDepositIntoOwnAccount() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(id);
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
                Arguments.of(5001, "Deposit amount cannot exceed 5000"),
                Arguments.of(0, "Deposit amount must be at least 0.01"),
                Arguments.of(-10, "Deposit amount must be at least 0.01"));
    }

    @MethodSource("sumInvalidData")
    @ParameterizedTest
    public void userCanNotMakeDepositIntoOwnAccountInvalidSum(Integer sum, String errorMessage) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(userRequest.getUsername(), userRequest.getPassword());
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(id, sum);
        CreateDepositRequest expectedResult = TestData.buildCreateDepositRequest(id, 0);
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
        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(0);
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
        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(id, 100);
        CreateDepositRequest expectedResult = TestData.buildCreateDepositRequest(id, 0);
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
