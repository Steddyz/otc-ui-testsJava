package com.example.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.JavascriptExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class MarketplaceSearchPage {

    public void openSearchResults(String query) {
        String url = "https://etp.otc.ru/marketplace-b2b/query/" + query;
        System.out.println("Открываем: " + url);
        Selenide.open(url);

        $("body").shouldBe(visible);

        System.out.println("Страница загружена");
    }

    public void saveProductsToFile(String filename) throws IOException {
        try {
            JavascriptExecutor js = (JavascriptExecutor) WebDriverRunner.getWebDriver();
            String pageHtml = (String) js.executeScript("return document.documentElement.outerHTML;");

            Files.write(Paths.get(filename), pageHtml.getBytes());

            System.out.println("Текст страницы сохранен в: " + filename);
            System.out.println("Длина текста: " + pageHtml.length() + " символов");
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении: " + e.getMessage());

            try {
                String pageText = Selenide.$("body").getText();
                Files.write(Paths.get(filename), pageText.getBytes());
                System.out.println("Текст страницы (через Selenide) сохранен в: " + filename);
                System.out.println("Длина текста: " + pageText.length() + " символов");
            } catch (Exception e2) {
                String errorMsg = "Не удалось получить содержимое страницы. URL: " +
                        WebDriverRunner.getWebDriver().getCurrentUrl();
                Files.write(Paths.get(filename), errorMsg.getBytes());
                System.out.println("Сохранена только информация об ошибке");
            }
        }
    }
}