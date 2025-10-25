package interation1.ui;

import api.requests.steps.AdminSteps;
import com.codeborne.selenide.*;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserTest extends BaseUiTest {

    @Test
    public void adminCanCreateUserTest() {

        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        authAsUser(admin);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.visible);

        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername())).count();
        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}
