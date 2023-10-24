package com.example.cardetail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class CarViewModel extends AndroidViewModel {

    private CarRepository repository;
    private LiveData<List<Car>> allCars;


    public CarViewModel(@NonNull Application application) {
        super(application);
        repository = new CarRepository(application);
        allCars = repository.getAllcars();
    }

    public void insert(Car car) {
        repository.insert(car);
    }

    public void delete(Car car) {
        repository.delete(car);
    }

    public void updateCar(Car car) {
        repository.updateCar(car);
    }

    public void deleteAll() {
        repository.deleteAllCars();
    }

    public LiveData<List<Car>> getAllCars() {
        return allCars;
    }

}
