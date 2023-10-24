package com.example.cardetail;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CarRepository {
    private CarDao carDao;
    private LiveData<List<Car>> allcars;

    public CarRepository(Application application) {
        CarDatabase database = CarDatabase.getInstance(application);
        carDao = database.carDao();
        allcars = carDao.getAllCars();
    }

    // These methods are the APIs which are exposed to the outside to perform the operations
    //these are user generated APIs
    // which will be called by the ViewModelClass
    public void insert(Car car) {
        new InsertCarAsyncTask(carDao).execute(car);
    }

    public void updateCar(Car car) {
        new UpdateCarAsyncTask(carDao).execute(car);
    }

    public void delete(Car car) {
        new DeleteCarAsyncTask(carDao).execute(car);
    }

    public void deleteAllCars() {
        new DeleteAllCarAsyncTask(carDao).execute();
    }

    public LiveData<List<Car>> getAllcars() {
        return allcars;
    }
    // \\

    private static class InsertCarAsyncTask extends AsyncTask<Car, Void, Void> {

        private CarDao carDao;

        private InsertCarAsyncTask(CarDao carDao) {
            this.carDao = carDao;
        }

        @Override
        protected Void doInBackground(Car... cars) {
            carDao.insert(cars[0]);
            return null;
        }
    }


    private static class UpdateCarAsyncTask extends AsyncTask<Car, Void, Void> {

        private CarDao carDao;

        private UpdateCarAsyncTask(CarDao carDao) {
            this.carDao = carDao;
        }

        @Override
        protected Void doInBackground(Car... cars) {
            carDao.update(cars[0]);
            return null;
        }
    }

    private static class DeleteCarAsyncTask extends AsyncTask<Car, Void, Void> {

        private CarDao carDao;

        private DeleteCarAsyncTask(CarDao carDao) {
            this.carDao = carDao;
        }

        @Override
        protected Void doInBackground(Car... cars) {
            carDao.delete(cars[0]);
            return null;
        }
    }

    private static class DeleteAllCarAsyncTask extends AsyncTask<Void, Void, Void> {

        private CarDao carDao;

        private DeleteAllCarAsyncTask(CarDao carDao) {
            this.carDao = carDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            carDao.deleteAllCars();
            return null;
        }
    }

}
