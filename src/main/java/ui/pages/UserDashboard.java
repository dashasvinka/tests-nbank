package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.Getter;
import static ui.utils.AllureUtils.attachScreenshot;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {

    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoney = $(Selectors.byText("💰 Deposit Money"));
    private SelenideElement makeTransfer = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    @Step("Выбрать опцию Создать новый аккаунт")
    public UserDashboard createNewAccount() {
        createNewAccount.click();
        attachScreenshot("Скриншот после выбора опции 'Создать новый аккаунт'"); // <--- скриншот
        return this;
    }

    @Step("Выбрать опцию Создать новый депозит")
    public UserDashboard depositMoney() {
        depositMoney.click();
        attachScreenshot("Скриншот после выбора опции 'Создать новый депозит'"); // <--- скриншот
        return this;
    }

    @Step("Выбрать опцию Создать новый трансфер")
    public UserDashboard makeTransfer() {
        makeTransfer.click();
        attachScreenshot("Скриншот после выбора опции 'Создать новый трансфер'"); // <--- скриншот
        return this;
    }
}
