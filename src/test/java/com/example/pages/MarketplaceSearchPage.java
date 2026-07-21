package com.example.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.example.dto.ProductDto;
import com.example.utils.TestLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class MarketplaceSearchPage {

    public void openSearchResults(String url) {
        TestLogger.info("Открываем: {}", url);
        Selenide.open(url);

        $("body").shouldBe(visible);

        $$("div[class*='ProductCard-module']").first().shouldBe(visible);

        TestLogger.info("Страница загружена");
    }

    public List<ProductDto> getProducts() {
        List<ProductDto> products = new ArrayList<>();

        try {
            ElementsCollection productCards = $$("div[class*='ProductCard-module']")
                    .filter(visible);

            TestLogger.info("Найдено карточек: {}", productCards.size());

            for (var card : productCards) {
                String name = "";
                String price = "Цена не указана";
                String city = "Город не указан";
                String saleType = "Тип не указан";

                try {
                    // Ищем название товара
                    var nameLink = card.$("a[href*='/marketplace-b2b/offer/']");
                    if (nameLink.exists()) {
                        name = nameLink.getText().trim();
                    } else {
                        String cardText = card.getText();
                        String[] lines = cardText.split("\n");
                        for (String line : lines) {
                            String trimmedLine = line.trim();
                            if (!trimmedLine.isEmpty() &&
                                    !trimmedLine.contains("₽") &&
                                    !trimmedLine.contains("/шт") &&
                                    !trimmedLine.contains("/WT") &&
                                    !trimmedLine.contains("Опт") &&
                                    !trimmedLine.contains("Розница") &&
                                    !trimmedLine.equals("Заказать") &&
                                    !trimmedLine.equals("В наличии") &&
                                    !trimmedLine.equals("Руб/Штука") &&
                                    !trimmedLine.equals("Цена") &&
                                    !trimmedLine.equals("Краснодар") &&
                                    trimmedLine.length() > 3) {
                                name = trimmedLine;
                                break;
                            }
                        }
                    }

                    if (name.isEmpty() || name.length() < 3) {
                        continue;
                    }

                    // Ищем цену
                    var priceElement = card.$("h3[class*='Title-module']");
                    if (priceElement.exists()) {
                        price = priceElement.getText().trim();
                    } else {
                        String fullText = card.getText();
                        String[] lines = fullText.split("\n");
                        for (String line : lines) {
                            if (line.contains("₽")) {
                                price = line.trim();
                                break;
                            }
                        }
                    }

                    // Ищем город и тип продажи
                    String fullText = card.getText();
                    String[] lines = fullText.split("\n");

                    for (String line : lines) {
                        String trimmedLine = line.trim();
                        if (trimmedLine.isEmpty()) continue;

                        if (trimmedLine.contains("Опт") || trimmedLine.contains("Розница")) {
                            saleType = trimmedLine;
                        } else if (trimmedLine.contains("₽")) {
                            continue;
                        } else if (!trimmedLine.contains("₽") &&
                                !trimmedLine.contains("/шт") &&
                                !trimmedLine.contains("/WT") &&
                                !trimmedLine.contains("Опт") &&
                                !trimmedLine.contains("Розница") &&
                                !trimmedLine.startsWith("http") &&
                                !trimmedLine.equals("Заказать") &&
                                !trimmedLine.equals("В наличии") &&
                                !trimmedLine.equals("Руб/Штука") &&
                                !trimmedLine.equals("Цена") &&
                                !trimmedLine.equals(name) &&
                                trimmedLine.length() < 30) {
                            city = trimmedLine;
                            break;
                        }
                    }

                    products.add(new ProductDto(name, price, city, saleType));

                } catch (Exception e) {
                    TestLogger.warn("Ошибка при парсинге карточки: {}", e.getMessage());
                }
            }

            TestLogger.info("Обработано товаров: {}", products.size());

        } catch (Exception e) {
            TestLogger.error("Ошибка: {}", e.getMessage());
        }

        return products;
    }

    public void saveProductsToFile(String filename) throws IOException {
        List<ProductDto> products = getProducts();
        List<String> productsInfo = new ArrayList<>();
        productsInfo.add("=== СПИСОК ТОВАРОВ (ПРИНТЕРЫ) ===");
        productsInfo.add("Дата: " + java.time.LocalDateTime.now());
        productsInfo.add("---");

        int counter = 0;
        for (ProductDto product : products) {
            counter++;
            productsInfo.add(counter + ". " + product.getName());
            productsInfo.add("   Цена: " + product.getPrice());
            productsInfo.add("   Город: " + product.getCity());
            productsInfo.add("   Тип продажи: " + product.getSaleType());
            productsInfo.add("");
        }

        if (products.isEmpty()) {
            productsInfo.add("Товары не найдены на странице");
        }

        String content = String.join("\n", productsInfo);
        Files.writeString(Paths.get(filename), content, StandardCharsets.UTF_8);

        TestLogger.info("Файл сохранен: {}", filename);
        TestLogger.info("Всего сохранено товаров: {}", counter);
    }
}