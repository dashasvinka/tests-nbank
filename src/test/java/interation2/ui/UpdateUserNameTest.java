package interation2.ui;

import api.models.GetProfileInfoResponse;
import api.models.UpdateNameRequest;
import api.requests.steps.UserSteps;
import api.generators.RandomModelGenerator;
import api.models.comparison.ModelAssertions;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import interation1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import api.requests.steps.TestData;
import ui.pages.BankAlert;
import ui.pages.EditProfile;

public class UpdateUserNameTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanUpdateNameWithCorrectData() {
        UpdateNameRequest updateNameRequest = RandomModelGenerator.generate(UpdateNameRequest.class);
        new EditProfile().open().editUserName(updateNameRequest.getName())
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .open().checkUpdatedName(updateNameRequest.getName());
        GetProfileInfoResponse getProfileInfoResponse = UserSteps.getProfileInfo(SessionStorage.getUser(1).getUsername(), SessionStorage.getUser(1).getPassword());
        ModelAssertions.assertThatModels(updateNameRequest, getProfileInfoResponse).match();
    }

    @Test
    @UserSession
    public void userCannotUpdateNameWithIncorrectData() {
        UpdateNameRequest updateNameRequest = RandomModelGenerator.generate(UpdateNameRequest.class);
        updateNameRequest.setName("ariana");
        new EditProfile().open().editUserName(updateNameRequest.getName())
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS.getMessage(),BankAlert.PLEASE_ENTER_VALID_NAME.getMessage())
                .open().checkNameHasNotUpdated();
        UpdateNameRequest expectedResult = TestData.buildUpdateNameRequest(null);
        GetProfileInfoResponse getProfileInfoResponse = UserSteps.getProfileInfo(SessionStorage.getUser(1).getUsername(), SessionStorage.getUser(1).getPassword());
        ModelAssertions.assertThatModels(expectedResult, getProfileInfoResponse).match();
    }
}
