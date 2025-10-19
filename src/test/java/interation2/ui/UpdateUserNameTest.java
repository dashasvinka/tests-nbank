package interation2.ui;

import api.models.CreateUserRequest;
import api.models.GetProfileInfoResponse;
import api.models.UpdateNameRequest;
import api.requests.steps.UserSteps;
import api.generators.RandomModelGenerator;
import api.models.comparison.ModelAssertions;
import interation1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import api.requests.steps.TestData;
import ui.pages.BankAlert;
import ui.pages.EditProfile;

public class UpdateUserNameTest extends BaseUiTest {

    @Test
    public void userCanUpdateNameWithCorrectData() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        UpdateNameRequest updateNameRequest = RandomModelGenerator.generate(UpdateNameRequest.class);
        new EditProfile().open().editUserName(updateNameRequest.getName())
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .open().checkUpdatedName(updateNameRequest.getName());
        GetProfileInfoResponse getProfileInfoResponse = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        ModelAssertions.assertThatModels(updateNameRequest, getProfileInfoResponse).match();
    }

    @Test
    public void userCannotUpdateNameWithInсorrectData() {
        CreateUserRequest user = AdminSteps.createUser();
        UpdateNameRequest updateNameRequest = RandomModelGenerator.generate(UpdateNameRequest.class);
        updateNameRequest.setName("ariana");
        authAsUser(user);
        new EditProfile().open().editUserName(updateNameRequest.getName())
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS.getMessage())
                .open().checkNameHasNotUpdated();
        UpdateNameRequest expectedResult = TestData.buildUpdateNameRequest(null);
        GetProfileInfoResponse getProfileInfoResponse = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        ModelAssertions.assertThatModels(expectedResult, getProfileInfoResponse).match();
    }
}
