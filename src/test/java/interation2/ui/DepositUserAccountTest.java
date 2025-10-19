package interation2.ui;

import api.models.*;
import api.generators.RandomModelGenerator;
import api.models.comparison.ModelAssertions;
import interation1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.ProfileInfoSteps;
import api.requests.steps.TestData;
import ui.pages.DepositMoneyPage;
import ui.pages.UserDashboard;
import static ui.pages.BankAlert.DEPOSIT_LESS_5000;

public class DepositUserAccountTest extends BaseUiTest {

    @Test
    public void userCanMakeValidDeposit() {
        CreateUserRequest user = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(id);
        String idForDeposit =  Long.toString(createDepositRequest.getId());
        String balance =  Double.toString(createDepositRequest.getBalance());

        authAsUser(user);
        new UserDashboard().open().depositMoney().getPage(DepositMoneyPage.class).createDeposit(idForDeposit, balance)
                .checkSuccessDepositAlert(id, createDepositRequest.getBalance());
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, id);
        ModelAssertions.assertThatModels(account, createDepositRequest).match();
    }

    @Test
    public void userCannotMakeInvalidDeposit() {
        CreateUserRequest user = AdminSteps.createUser();
        Long id = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(id);
        createDepositRequest.setBalance(40000);
        String idForDeposit =  Long.toString(createDepositRequest.getId());
        String balance =  Double.toString(createDepositRequest.getBalance());

        authAsUser(user);
        new UserDashboard().open().depositMoney().getPage(DepositMoneyPage.class).createDeposit(idForDeposit, balance)
                .checkAlertMessageAndAccept(DEPOSIT_LESS_5000.getMessage());
        CreateDepositRequest expectedResult = TestData.buildCreateDepositRequest(id, 0);
        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, id);
        ModelAssertions.assertThatModels(account, expectedResult).match();
    }
}
