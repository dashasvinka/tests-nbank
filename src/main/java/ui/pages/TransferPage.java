package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Alert;
import ui.utils.RetryUtils;

import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static ui.pages.BankAlert.SUCCESSFULLY_TRANSFERRED;
import static ui.utils.AllureUtils.attachScreenshot;

public class TransferPage extends BasePage<TransferPage> {
    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement recipientAccountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement selectAcc = $(Selectors.byClassName("form-group")).find("select");
    private SelenideElement confirmCheckButton = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    @Step("Создать трансфер")
    public TransferPage createTransfer(String amount, String idRecipientAccount, String idAccount, Boolean needToConfirm) {
        amountInput.sendKeys(amount);
        recipientAccountInput.sendKeys(idRecipientAccount);
        selectAcc.click();
        RetryUtils.retry(
                "Выбор счета для трансфера после его появления в списке для выбора",
                () -> {
                    var option = $(Selectors.byValue(idAccount));
                    option.click();
                    return option.isSelected();
                },
                clicked -> clicked,
                5,
                500
        );
        if (needToConfirm) {
            confirmCheckButton.click();
        }
        sendTransferButton.click();
        return this;
    }

    @Step("Проверить нотификацию об успешном трансфере")
    public TransferPage checkSuccessTransferAlert(Long idSecond, Double amount) {
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains(SUCCESSFULLY_TRANSFERRED.getMessage());
        alert.accept();
        var m = Pattern.compile("(?<=ACC)\\d+").matcher(alertText);
        m.find();
        assert Long.parseLong(m.group()) == idSecond;
        var p = Pattern.compile("(?<=\\$)\\d+(?:\\.\\d+)?").matcher(alertText);
        p.find();
        assertThat(Double.parseDouble(p.group()))
                .isCloseTo(amount, within(0.0001));

        attachScreenshot("Скриншот после проверки успешного трансфера");

        return this;
    }

    @Override
    public String url() {
        return "/transfer";
    }
}
