package interation2.api;

import api.generators.RandomModelGenerator;
import api.models.AccountModel;
import api.models.CreateDepositRequest;
import api.models.CreateUserRequest;
import api.models.GetProfileInfoResponse;
import common.annotations.KnownIssue;
import common.extensions.KnownIssueExtension;
import interation1.api.BaseTest;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.ProfileInfoSteps;
import api.requests.steps.TestData;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
    @ExtendWith(KnownIssueExtension.class)
    @KnownIssue(
            ticket = "DEFECT-0001",
            description = "DEFECT: Backend returns 200 instead of 400 only for deposit sum = 5001",
            onlyForArgs = {"5001"}
    )
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
