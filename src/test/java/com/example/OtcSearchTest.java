package com.example;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.example.pages.MainPage;
import com.example.pages.MarketplaceSearchPage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class OtcSearchTest {

    private static String siteUrl;
    private static String searchQuery;
    private static String searchCity;
    private static String searchUrl;

    @BeforeAll
    public static void setup() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
            props.load(fis);
        }

        siteUrl = props.getProperty("site.url");
        searchQuery = props.getProperty("search.query");
        searchCity = props.getProperty("search.city");
        searchUrl = props.getProperty("search.url"); // Загружаем search.url

        Configuration.browser = "edge";
        Configuration.headless = false;
        Configuration.timeout = 30000;
        Configuration.browserSize = "1920x1080";

        System.setProperty("selenide.timeout", "30000");
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    public void siteLoadTest() {
        MainPage mainPage = new MainPage();
        mainPage.open(siteUrl);

        String pageTitle = WebDriverRunner.getWebDriver().getTitle();
        assertNotNull(pageTitle, "Заголовок страницы отсутствует!");
        assertFalse(pageTitle.isEmpty(), "Заголовок страницы пустой!");
        System.out.println("Заголовок страницы: " + pageTitle);

        String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
        assertNotNull(currentUrl, "URL отсутствует!");
        assertTrue(currentUrl.contains("market.otc.ru"), "Неправильный URL!");
        System.out.println("Текущий URL: " + currentUrl);
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    public void mainPageElementsTest() {
        MainPage mainPage = new MainPage();
        mainPage.open(siteUrl);

        assertTrue(mainPage.hasSearchGoodsLink(), "Ссылка 'Поиск товаров' не найдена!");
        System.out.println("Ссылка 'Поиск товаров' найдена");

        assertTrue(mainPage.hasLoginButton(), "Кнопка 'Вход' не найдена!");
        System.out.println("Кнопка 'Вход' найдена");

        assertTrue(mainPage.hasRegisterButton(), "Кнопка 'Регистрация' не найдена!");
        System.out.println("Кнопка 'Регистрация' найдена");
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    public void searchProductTest() throws IOException {
        // Используем searchUrl вместо создания MainPage и перехода по ссылке
        MarketplaceSearchPage searchPage = new MarketplaceSearchPage();
        searchPage.openSearchResults(searchUrl); // Передаём готовую ссылку
        searchPage.saveProductsToFile("storProducts.txt");

        System.out.println("Тест успешно завершен!");
        System.out.println("Файл сохранен как: storProducts.txt");
    }
}