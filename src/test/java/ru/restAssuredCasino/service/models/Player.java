package ru.restAssuredCasino.service.models;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

public class Player {

    private String id;
    private String username;
    private String password;
    private String email;
    private String name;
    private String surname;
    private Currency currency;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordBase64() {
        return Base64.getEncoder().encodeToString(this.password.getBytes());
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Player()
    {
    }

    public Player(String id, String username, String password, String email, String name, String surname, Currency currency)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.currency = currency;
    }

    public Player RandomPlayer()
    {
        int rnd = new Random().nextInt(Currency.getAvailableCurrencies().toArray().length);
        this.username = RandomStringUtils.randomAlphabetic(10);
        this.password = RandomStringUtils.randomAlphanumeric(10);
        this.email = RandomStringUtils.randomAlphabetic(10) + "@gmail.com";
        this.name = RandomStringUtils.randomAlphabetic(6);
        this.surname = RandomStringUtils.randomAlphabetic(10);
        this.currency = (Currency) Currency.getAvailableCurrencies().toArray()[rnd];
        return this;
    }

    public Map<String, Object> getPlayerJson()
    {
        Map<String, Object> playerJson = new HashMap<>();
        playerJson.put("username",getUsername());
        playerJson.put("password_change",getPasswordBase64());
        playerJson.put("password_repeat",getPasswordBase64());
        playerJson.put("email",this.email);
        playerJson.put("name",this.name);
        playerJson.put("surname",this.surname);
        playerJson.put("currency_code",this.currency.getCurrencyCode());
        return playerJson;
    }
}
