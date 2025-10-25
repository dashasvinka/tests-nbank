package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class EditProfile extends BasePage<EditProfile> {

    private SelenideElement newNameInput =  $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton =  $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    private SelenideElement noNameLabel =   $(Selectors.byText("Noname"));
    @Override
    public String url(){
        return "/edit-profile";
    }

    public EditProfile editUserName(String newName){
        newNameInput.sendKeys(newName);
        saveChangesButton.click();
        return this;
    }

    public void checkUpdatedName(String newName){
        $(Selectors.byText(newName)).shouldBe(Condition.visible);
    }

    public void checkNameHasNotUpdated(){
        noNameLabel.shouldBe(Condition.visible);
    }
}
