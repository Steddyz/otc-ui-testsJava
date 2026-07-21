package com.example.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
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

    public boolean hasSecondPage() {
        SelenideElement secondPageLink = $("a[href*='page=2'][class*='PaginationBlock-module']");
        return secondPageLink.exists() && secondPageLink.isDisplayed();
    }

    public void goToSecondPage() {
        TestLogger.info("Переход на вторую страницу...");

        SelenideElement secondPageLink = $("a[href*='page=2'][class*='PaginationBlock-module']");

        if (secondPageLink.exists() && secondPageLink.isDisplayed()) {
            TestLogger.info("Найдена ссылка на вторую страницу");
            secondPageLink.click();
            TestLogger.info("Переход на вторую страницу выполнен");

            $$("div[class*='ProductCard-module']").first().shouldBe(visible);
            Selenide.sleep(1000);
        } else {
            TestLogger.warn("Ссылка на вторую страницу не найдена");

            ElementsCollection paginationLinks = $$("a[class*='PaginationBlock-module']")
                    .filter(visible);

            SelenideElement secondPageByText = null;
            for (SelenideElement link : paginationLinks) {
                if ("2".equals(link.getText().trim())) {
                    secondPageByText = link;
                    break;
                }
            }

            if (secondPageByText != null && secondPageByText.exists()) {
                secondPageByText.click();
                TestLogger.info("Переход на вторую страницу выполнен (по тексту)");
                $$("div[class*='ProductCard-module']").first().shouldBe(visible);
                Selenide.sleep(1000);
            } else {
                TestLogger.warn("Кнопка '2' не найдена");
            }
        }
    }

    public List<ProductDto> getProducts() {
        List<ProductDto> products = new ArrayList<>();

        try {
            ElementsCollection productCards = $$("div[class*='ProductCard-module']")
                    .filter(visible);

            TestLogger.info("Найдено карточек на текущей странице: {}", productCards.size());

            for (int i = 0; i < productCards.size(); i++) {
                SelenideElement card = $$("div[class*='ProductCard-module']").get(i);

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
                    TestLogger.warn("Ошибка при парсинге карточки {}: {}", i, e.getMessage());
                }
            }

            TestLogger.info("Собрано товаров: {}", products.size());

        } catch (Exception e) {
            TestLogger.error("Ошибка: {}", e.getMessage());
        }

        return products;
    }

    public List<ProductDto> getProductsFromAllPages() {
        List<ProductDto> allProducts = new ArrayList<>();

        TestLogger.info("Сбор товаров с 1-й страницы...");
        allProducts.addAll(getProducts());

        if (hasSecondPage()) {
            goToSecondPage();

            TestLogger.info("Сбор товаров со 2-й страницы...");
            allProducts.addAll(getProducts());

            TestLogger.info("Всего собрано товаров с двух страниц: {}", allProducts.size());
        } else {
            TestLogger.info("ℹВторая страница не найдена, собрано только с первой страницы: {}", allProducts.size());
        }

        return allProducts;
    }

    public void saveProductsToFile(String filename) throws IOException {
        List<ProductDto> products = getProductsFromAllPages();

        List<String> productsInfo = new ArrayList<>();
        productsInfo.add("=== СПИСОК ТОВАРОВ (ПРИНТЕРЫ) ===");
        productsInfo.add("Дата: " + java.time.LocalDateTime.now());
        productsInfo.add("Всего товаров: " + products.size());
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