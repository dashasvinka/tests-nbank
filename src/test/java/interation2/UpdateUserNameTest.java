package interation2;

import generators.RandomData;
import interation1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.GetProfileInfoRequester;
import requests.UpdateUserNameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class UpdateUserNameTest extends BaseTest {

    @Test
    public void userCanUpdateNameWithCorrectData() {
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

        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name("Ivan Petrov")
                .build();
        // изменение имени пользователя
        new UpdateUserNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(updateNameRequest);
        // проверяем что имя изменилось
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);
        softly.assertThat("Ivan Petrov").isEqualTo(result.getName());
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

        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name(name)
                .build();

        // изменение имени пользователя
        new UpdateUserNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorMessage))
                .put(updateNameRequest);
        // проверяем что имя изменилось
        GetProfileInfoResponse result = new GetProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(GetProfileInfoResponse.class);
        softly.assertThat(result.getName()).isEqualTo(null);
    }
}
