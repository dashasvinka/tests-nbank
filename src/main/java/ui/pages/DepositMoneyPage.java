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
import static ui.pages.BankAlert.SUCCESSFULLY_DEPOSITED;
import static ui.utils.AllureUtils.attachScreenshot;

public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
    private SelenideElement selectAcc = $(Selectors.byClassName("form-group")).find("select");
    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    @Step("Создать депозит")
    public DepositMoneyPage createDeposit(String idForDeposit, String balance) {
        selectAcc.click();
        RetryUtils.retry(
                "Выбор счета для депозита после его появления",
                () -> {
                    var option = $(Selectors.byValue(idForDeposit));
                    option.click();
                    return option.isSelected();
                },
                clicked -> clicked,
                5,
                500
        );
        amountInput.sendKeys(balance);
        depositButton.click();
        return this;
    }

    @Step("Проверить нотификацию об успешно созданном депозите")
    public DepositMoneyPage checkSuccessDepositAlert(Long id, Double balance) {
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains(SUCCESSFULLY_DEPOSITED.getMessage());
        alert.accept();

        var m = Pattern.compile("(?<=ACC)\\d+").matcher(alertText);
        m.find();
        assert Long.parseLong(m.group()) == id;

        var p = Pattern.compile("(?<=\\$)\\d+(?:\\.\\d+)?").matcher(alertText);
        p.find();
        assertThat(Double.parseDouble(p.group()))
                .isCloseTo(balance, within(0.0001));

        attachScreenshot("Скриншот после проверки успешного депозита");

        return this;
    }
}
