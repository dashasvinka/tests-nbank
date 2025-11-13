package ui.utils;
import com.codeborne.selenide.Screenshots;
import io.qameta.allure.Allure;

import java.io.File;
import java.io.FileInputStream;

public class AllureUtils {
    public static void attachScreenshot(String name) {
        File screenshot = Screenshots.takeScreenShotAsFile();
        if (screenshot != null) {
            try (FileInputStream fis = new FileInputStream(screenshot)) {
                Allure.addAttachment(name, fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
