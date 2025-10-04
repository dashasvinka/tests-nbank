package interation2.api;

import generators.RandomModelGenerator;
import interation1.api.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.TestData;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class UpdateUserNameTest extends BaseTest {

    @Test
    public void userCanUpdateNameWithCorrectData() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        UpdateNameRequest updateNameRequest = RandomModelGenerator.generate(UpdateNameRequest.class);

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.UPDATE_NAME,
                ResponseSpecs.requestReturnsOK())
                .put(updateNameRequest);

        GetProfileInfoResponse getProfileInfoResponse = new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);

        ModelAssertions.assertThatModels(updateNameRequest, getProfileInfoResponse).match();
    }

    public static Stream<Arguments> nameInvalidData() {
        return Stream.of(
                Arguments.of("Vasia", "Name must contain two words with letters only"),
                Arguments.of("Vasia_Vasiliev", "Name must contain two words with letters only"),
                Arguments.of("@Vasia #Vasiliev", "Name must contain two words with letters only"));
    }
    @MethodSource("nameInvalidData")
    @ParameterizedTest
    public void userCanNotUpdateNameWithInvalidData(String name, String errorMessage) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UpdateNameRequest updateNameRequest = TestData.buildUpdateNameRequest(name);
        UpdateNameRequest expectedResult = TestData.buildUpdateNameRequest(null);
        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.UPDATE_NAME,
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .put(updateNameRequest);
        GetProfileInfoResponse getProfileInfoResponse = new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);
        ModelAssertions.assertThatModels(expectedResult, getProfileInfoResponse).match();
    }
}
