package com.example.cardetail;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CarDao {

    @Insert
    void insert(Car car);

    @Delete
    void delete(Car car);

    @Update
    void update(Car car);

    @Query("DELETE FROM car_table")
    void deleteAllCars();

    @Query("SELECT * FROM car_table ORDER BY id DESC")
    LiveData<List<Car>> getAllCars();

}
