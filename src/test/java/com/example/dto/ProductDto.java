package com.example.dto;

import java.util.Objects;

public class ProductDto {
    private String name;
    private String price;
    private String city;
    private String saleType;

    public ProductDto() {
    }

    public ProductDto(String name, String price, String city, String saleType) {
        this.name = name;
        this.price = price;
        this.city = city;
        this.saleType = saleType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    @Override
    public String toString() {
        return name + ", " + price + ", " + city + ", " + saleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDto that = (ProductDto) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(price, that.price) &&
                Objects.equals(city, that.city) &&
                Objects.equals(saleType, that.saleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, city, saleType);
    }
}