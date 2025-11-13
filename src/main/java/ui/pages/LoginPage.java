package ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;
import static ui.utils.AllureUtils.attachScreenshot;

public class LoginPage extends BasePage<LoginPage>{
    private SelenideElement button = $("button");
    @Override
    public String url(){
        return "/login";
    }

    @Step("Осуществить логин")
    public LoginPage login(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        button.click();

        attachScreenshot("Скриншот после логина пользователя: " + username);

        return this;
    }
}

