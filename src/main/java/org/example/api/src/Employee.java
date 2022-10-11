package org.example.api.src;

public class Employee {
    private int id;
    private String name;
    private String address;
    private int age;
    private long salary;

    public void setID(int id){
        this.id = id;
    }
    public int getID(){
        return id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public String getAddress(){
        return address;
    }
    public void setAge(int age){
        this.age = age;
    }
    public int getAge(){
        return age;
    }
    public void setSalary(long salary){
        this.salary = salary;
    }
    public long getSalary(){
        return salary;
    }
    public Employee(int id, String name, String address, int age, long salary){
        super();
        this.id = id;
        this.name = name;
        this.address = address;
        this.age = age;
        this.salary = salary;
    }
}
