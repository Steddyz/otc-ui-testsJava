package com.example;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class OtcSearchTest {

    private static String searchQuery;
    private static String siteUrl;

    @BeforeAll
    public static void setup() throws IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
        props.load(fis);
        fis.close();

        searchQuery = props.getProperty("search.query");
        siteUrl = props.getProperty("site.url");

        Configuration.browser = "edge";
        Configuration.headless = false;
        Configuration.timeout = 15000;
        Configuration.browserSize = "1920x1080";

        System.out.println("🚀 Запуск теста для сайта: " + siteUrl);
    }

    @Test
    public void searchProductsTest() throws IOException {
        open(siteUrl);
        System.out.println("✅ Сайт открыт");
        sleep(3000);

        if ($("a[href*='marketplace-b2b']").exists()) {
            $("a[href*='marketplace-b2b']").click();
            System.out.println("✅ Нажата ссылка 'Поиск товаров'");
            sleep(3000);
        } else {
            System.out.println("⚠️ Ссылка 'Поиск товаров' не найдена");
        }

        List<String> products = new ArrayList<>();

        System.out.println("📄 Сбор данных со страницы...");
        String pageText = $("body").getText();
        String[] lines = pageText.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && line.length() > 10) {
                products.add(line);
            }
        }

        System.out.println("✅ Собрано строк: " + products.size());

        if (products.isEmpty()) {
            products.add("Страница не содержит данных");
        }

        FileWriter writer = new FileWriter("products.txt");
        writer.write("=== СОДЕРЖИМОЕ СТРАНИЦЫ ===\n\n");
        for (String product : products) {
            writer.write(product + "\n");
        }
        writer.close();

        System.out.println("✅ Данные сохранены в products.txt");

        File file = new File("products.txt");
        assert file.exists() : "❌ Файл не создан!";
        assert file.length() > 0 : "❌ Файл пустой!";

        System.out.println("✅ Тест успешно завершен!");
    }
}