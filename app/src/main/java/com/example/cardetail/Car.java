package com.example.cardetail;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


// Car Entities

@Entity(tableName = "car_table")
public class Car {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String car_make;
    private String car_model;

    // for saving image
    private String image;


    public Car(String car_make, String car_model,String image) {
        this.car_make = car_make;
        this.car_model = car_model;
        this.image = image;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getCar_make() {
        return car_make;
    }

    public String getCar_model() {
        return car_model;
    }

    //image
    public String getImage() {
        return image;
    }

}
