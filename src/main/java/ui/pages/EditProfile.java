package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ui.utils.RetryUtils;

import static com.codeborne.selenide.Selenide.$;
import static ui.utils.AllureUtils.attachScreenshot;

public class EditProfile extends BasePage<EditProfile> {

    private SelenideElement newNameInput =  $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton =  $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    private SelenideElement noNameLabel =   $(Selectors.byText("Noname"));
    @Override
    public String url(){
        return "/edit-profile";
    }

    @Step("Изменить пользовательское наименование")
    public EditProfile editUserName(String newName) {
        RetryUtils.retry(
                "Ввод пользовательского имени и проверка его отображения",
                () -> {
                    newNameInput.clear();
                    newNameInput.sendKeys(newName);
                    return newNameInput.getAttribute("value");
                },
                enteredText -> newName.equals(enteredText),
                5,
                500
        );
        saveChangesButton.click();
        return this;
    }

    @Step("Проверить измененное пользовательское наименование")
    public void checkUpdatedName(String newName) {
        $(Selectors.byText(newName)).shouldBe(Condition.visible);
        attachScreenshot("Скриншот после проверки измененного имени: " + newName);
    }

    @Step("Проверить неизмененное пользовательское наименование")
    public void checkNameHasNotUpdated() {
        noNameLabel.shouldBe(Condition.visible);
        attachScreenshot("Скриншот после проверки неизмененного имени");
    }
}
