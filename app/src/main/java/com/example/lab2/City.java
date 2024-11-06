package com.example.lab2;

import java.util.HashMap;
import java.util.Map;

public class City {
    private String name;
    private String country;
    private int population;

    public City() {}

    public City(String name, String country, int population) {
        this.name = name;
        this.country = country;
        this.population = population;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("country", country);
        map.put("population", population);
        return map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
}
