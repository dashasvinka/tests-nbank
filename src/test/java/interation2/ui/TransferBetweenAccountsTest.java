package interation2.ui;

import api.generators.RandomModelGenerator;
import api.models.*;
import api.requests.steps.*;
import api.models.comparison.ModelAssertions;
import interation1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;
import static ui.pages.BankAlert.*;

public class TransferBetweenAccountsTest extends BaseUiTest {

    @Test
    public void userCanMakeValidTransferTest() {
        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(idFirst);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, createDepositRequest.getBalance()/2);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,(int) (createDepositRequest.getBalance() - createDepositRequest.getBalance()/2));
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,(int) (createDepositRequest.getBalance()/2));

        String amount =  Double.toString(createTransferRequest.getAmount());
        String idRecipientAccount =  "ACC" + Long.toString(createTransferRequest.getReceiverAccountId());
        String idAccount =  Long.toString(createDepositRequest.getId());

        authAsUser(user);
        new UserDashboard().open().makeTransfer().getPage(TransferPage.class).createTransfer(amount, idRecipientAccount, idAccount, true)
                .checkSuccessTransferAlert(idSecond, createTransferRequest.getAmount());

        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userMustConfirmTransferTest() {
        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());

        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,3000);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 1000);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,3000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);

        String amount =  Double.toString(createTransferRequest.getAmount());
        String idRecipientAccount =  "ACC" + Long.toString(createTransferRequest.getReceiverAccountId());
        String idAccount =  Long.toString(createDepositRequest.getId());

        authAsUser(user);
        new UserDashboard().open().makeTransfer().getPage(TransferPage.class).createTransfer(amount, idRecipientAccount, idAccount, false)
                .checkAlertMessageAndAccept(PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userCannotTransferForInvalidAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());

        CreateDepositRequest createDepositRequest  = RandomModelGenerator.generate(CreateDepositRequest.class);
        createDepositRequest.setId(idFirst);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, createDepositRequest.getBalance()/2);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,(int) (createDepositRequest.getBalance()));
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);

        String amount =  Double.toString(createTransferRequest.getAmount());
        String idRecipientAccount = Long.toString(createTransferRequest.getReceiverAccountId());
        String idAccount =  Long.toString(createDepositRequest.getId());

        authAsUser(user);
        new UserDashboard().open().makeTransfer().getPage(TransferPage.class).createTransfer(amount, idRecipientAccount, idAccount, true)
                .checkAlertMessageAndAccept(NO_USER_FOUND_WITH_ACCOUNT.getMessage());

        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }

    @Test
    public void userCannotTransferMoreThan10000Test() {
        CreateUserRequest user = AdminSteps.createUser();
        Long idFirst = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());
        Long idSecond = AccountSteps.createAccountAndGetId(user.getUsername(), user.getPassword());

        CreateDepositRequest createDepositRequest = TestData.buildCreateDepositRequest(idFirst,3000);
        DepositSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);
        CreateTransferRequest createTransferRequest = TestData.buildCreateTransferRequest(idFirst, idSecond, 100000);
        CreateDepositRequest expectedSender = TestData.buildCreateDepositRequest(idSecond,3000);
        CreateDepositRequest expectedReceiver = TestData.buildCreateDepositRequest(idFirst,0);

        String amount =  Double.toString(createTransferRequest.getAmount());
        String idRecipientAccount = "ACC" + Long.toString(createTransferRequest.getReceiverAccountId());
        String idAccount =  Long.toString(createDepositRequest.getId());

        authAsUser(user);
        new UserDashboard().open().makeTransfer().getPage(TransferPage.class).createTransfer(amount, idRecipientAccount, idAccount, true)
                .checkAlertMessageAndAccept(ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        GetProfileInfoResponse result = ProfileInfoSteps.getProfile(user.getUsername(), user.getPassword());
        AccountModel account = AccountSteps.findAccountById(result, idSecond);
        AccountModel accountSender = AccountSteps.findAccountById(result, idFirst);
        ModelAssertions.assertThatModels(account, expectedReceiver).match();
        ModelAssertions.assertThatModels(accountSender, expectedSender).match();
    }
}
