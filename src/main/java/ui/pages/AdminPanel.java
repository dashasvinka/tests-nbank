package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import io.qameta.allure.Step;
import lombok.Getter;
import ui.elements.UserBage;
import ui.utils.RetryUtils;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static ui.utils.AllureUtils.attachScreenshot;

@Getter
public class AdminPanel extends BasePage<AdminPanel>{
    private SelenideElement adminPanelText =  $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserButton = $(Selectors.byText("Add User"));
    @Override
    public String url(){
        return "/admin";
    }

    @Step("Создание пользователя")
    public AdminPanel createUser(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.click();

        attachScreenshot("Скриншот после создания пользователя");
        return this;
    }

    @Step("Получить всех пользователей")
    public List<UserBage> getAllUsers() {
        List<UserBage> result = StepLogger.log("Get all users from Dashboard", () -> {
            ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
            return generatePageElements(elementsCollection, UserBage::new);
        });

        attachScreenshot("Скриншот после получения списка пользователей");
        return result;
    }

    @Step("Найти пользователя по его имени")
    public UserBage findUserByUsername(String username) {
        UserBage user = RetryUtils.retry(
                "Проверка наличия пользователя с конкретным username",
                () -> getAllUsers().stream().filter(it -> it.getUsername().equals(username)).findAny().orElse(null),
                result -> result != null,
                3,
                1000
        );

        attachScreenshot("Скриншот после поиска пользователя: " + username);
        return user;
    }
}
