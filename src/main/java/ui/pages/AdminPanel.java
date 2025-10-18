package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
@Getter
public class AdminPanel extends BasePage<AdminPanel>{

    private SelenideElement adminPanelText =  $(Selectors.byText("Admin Panel"));
    @Override
    public String url(){
        return "/admin";
    }

}
