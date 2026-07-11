package com.example.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

public class MainPage {

    private final SelenideElement logo = $(".page-logo");
    private final SelenideElement searchGoodsLink = $("a[href*='marketplace-b2b']");
    private final SelenideElement loginButton = $("a[href*='Login']");
    private final SelenideElement registerButton = $("a[href*='Accreditation']");

    public MainPage open(String url) {
        Selenide.open(url);
        logo.shouldBe(visible);
        System.out.println("Главная страница открыта: " + url);
        return this;
    }

    public MarketplaceSearchPage goToMarketplaceB2b() {
        searchGoodsLink.shouldBe(visible, enabled);

        String href = searchGoodsLink.getAttribute("href");
        System.out.println("Ссылка 'Поиск товаров': " + href);

        searchGoodsLink.click();

        switchToNewTab();

        System.out.println("Переход на marketplace-b2b выполнен");
        return new MarketplaceSearchPage();
    }

    private void switchToNewTab() {
        String originalWindow = WebDriverRunner.getWebDriver().getWindowHandle();

        Selenide.Wait().until(driver -> driver.getWindowHandles().size() > 1);

        for (String windowHandle : WebDriverRunner.getWebDriver().getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                WebDriverRunner.getWebDriver().switchTo().window(windowHandle);
                break;
            }
        }
        System.out.println("Переключились на новую вкладку");
    }

    public boolean hasSearchGoodsLink() {
        return searchGoodsLink.exists();
    }

    public boolean hasLoginButton() {
        return loginButton.exists();
    }

    public boolean hasRegisterButton() {
        return registerButton.exists();
    }
}