package org.example.api.src;

public class Customer {
    int id;
    public String name;
    public String city;
    public String country;

    public static Customer of(String name, String city, String country){
        Customer data = new Customer();
        //data.id = id;
        data.name = name;
        data.city = city;
        data.country = country;
        return data;
    }
}
