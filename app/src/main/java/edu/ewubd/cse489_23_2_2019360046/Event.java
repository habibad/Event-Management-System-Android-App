package edu.ewubd.cse489_23_2_2019360046;

public class Event {
    String id = "";
    String name = "";
    String place = "";
    long datetime = 0;
    int capacity = 0;
    double budget = 0;
    String email = "";
    String phone = "";
    String description = "";
    String eventType = "";

    public Event(String id, String name, String place, long datetime,int capacity,double budget,String email,String phone,String description, String eventType){
        this.id = id;
        this.name = name;
        this.place = place;
        this.datetime = datetime;
        this.capacity = capacity;
        this.budget = budget;
        this.email = email;
        this.phone = phone;
        this.description = description;
        this.eventType = eventType;
    }
}
