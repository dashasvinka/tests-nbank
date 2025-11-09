package interation1.ui;

import api.configs.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.TimingExtension;
import common.extensions.UserSessionExtension;
import interation1.api.BaseTest;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
@ExtendWith(TimingExtension.class)
public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.baseUrl = Config.getProperty("baseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");
        Configuration.headless = true;
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }
}
