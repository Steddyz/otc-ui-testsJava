package com.example.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;

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
        System.out.println(" Открываем: " + url);
        Selenide.open(url);

        $("body").shouldBe(visible);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(" Ожидание было прервано: " + e.getMessage());
        }

        System.out.println(" Страница загружена");
    }

    public void saveProductsToFile(String filename) throws IOException {
        List<String> productsInfo = new ArrayList<>();
        productsInfo.add("=== СПИСОК ТОВАРОВ (ПРИНТЕРЫ) ===");
        productsInfo.add("Дата: " + java.time.LocalDateTime.now());
        productsInfo.add("---");

        try {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(" Ожидание было прервано: " + e.getMessage());
            }

            ElementsCollection productCards = $$("div[class*='ProductCard-module']");
            System.out.println("🔍 Найдено карточек: " + productCards.size());

            int productCounter = 0;

            for (var card : productCards) {
                String name = "";
                String price = "Цена не указана";
                String city = "Город не указан";
                String saleType = "Тип не указан";

                try {
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

                    productCounter++;

                    productsInfo.add(productCounter + ". " + name);
                    productsInfo.add("   Цена: " + price);
                    productsInfo.add("   Город: " + city);
                    productsInfo.add("   Тип продажи: " + saleType);
                    productsInfo.add("");

                } catch (Exception e) {
                    System.out.println(" Ошибка при парсинге карточки: " + e.getMessage());
                }
            }

            System.out.println(" Обработано товаров: " + productCounter);

            if (productsInfo.size() <= 3) {
                productsInfo.add("Товары не найдены на странице");
            }

            String content = String.join("\n", productsInfo);
            Files.writeString(Paths.get(filename), content, StandardCharsets.UTF_8);

            System.out.println(" Файл сохранен: " + filename);
            System.out.println(" Всего сохранено товаров: " + productCounter);

        } catch (Exception e) {
            System.out.println(" Ошибка: " + e.getMessage());
            throw new IOException("Ошибка при сохранении данных", e);
        }
    }
}