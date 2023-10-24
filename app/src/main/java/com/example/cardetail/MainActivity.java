package com.example.cardetail;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private CarViewModel carViewModel;

    private Spinner spinner, spinner2;
    private ArrayList<Model> modelArrayList;
    private ArrayList<Model> modelArrayList_temp = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> names_temp = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseUser user;


    private static ProgressDialog mProgressDialog;

    private String car_make = null;
    private String car_model = null;

    private String car_image = "nulll";

    //    private Bitmap imageSources = null;
    private Car carUpdateImg;


    private Button add_data, logout;

    OnItemClick itemClick;

//    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        add_data = findViewById(R.id.add_button);
        logout = findViewById(R.id.logout);

        spinner = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);

        /// Authentication
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // if user is not logged in then it will return to login activity.
        if (user == null) {
            Intent i = new Intent(getApplicationContext(), Login.class);
            startActivity(i);
            finish();
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // for logging out
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getApplicationContext(), Login.class);
                startActivity(i);
                finish();
            }
        });
        /// \\\

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final CarAdapter carAdapter = new CarAdapter(itemClick);
        recyclerView.setAdapter(carAdapter);


        names.add(0, "Select the Car Make");
//        names_temp.add(0,"Select the Car Model");

        loadJson();

        carViewModel = new ViewModelProvider(this).get(CarViewModel.class);

        carViewModel.getAllCars().observe(this, new Observer<List<Car>>() {
            @Override
            public void onChanged(List<Car> cars) {
                carAdapter.setCars(cars);
//                Toast.makeText(MainActivity.this, "OnChanged", Toast.LENGTH_SHORT).show();
            }
        });

        add_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((car_make != null) && (car_model != null)) {
                    Toast.makeText(MainActivity.this, "Car Make: " + car_make + " Car Model: " + car_model, Toast.LENGTH_SHORT).show();
                    saveCar();
                } else {
                    Toast.makeText(MainActivity.this, "Please Select the Car Maker And Car Model", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // //original (working)
//        carAdapter.setOnItemClickListener(new OnItemClick() {
//            @Override
//            public void onClick(Car car) {
//                carViewModel.delete(car);
//            }
//        });

        carAdapter.setOnItemClickListener(new OnItemClick() {
            @Override
            public void onClick(Car car) {
                carViewModel.delete(car);
            }

            @Override
            public void onClickAdd(Car car) {

                carUpdateImg = car;
                getImg();
            }

        });

    }

    public void loadJson() {

        showSimpleProgressDialog(this, "Loading...", "Fetching Json", false);

        String url = "https://vpic.nhtsa.dot.gov/api/vehicles/getallmakes?format=json";
        RequestQueue queue = Volley.newRequestQueue(this);

        modelArrayList = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray dataArray = response.getJSONArray("Results");
                    for (int i = 0; i < dataArray.length(); i++) {

                        JSONObject dataObj = dataArray.getJSONObject(i);
                        String make_id = dataObj.getString("Make_ID");
                        String make_name = dataObj.getString("Make_Name");

                        Model model = new Model();
                        model.setCarId(make_id);
                        model.setCarName(make_name);

                        modelArrayList.add(model);
//                        Toast.makeText(MainActivity.this, ""+make_id+" "+make_name, Toast.LENGTH_SHORT).show();
                    }

                    for (int i = 0; i < modelArrayList.size(); i++) {
                        names.add(modelArrayList.get(i).getCarName());
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, names);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerArrayAdapter);
                    spinner.setOnItemSelectedListener(MainActivity.this);

                    removeSimpleProgressDialog();
                } catch (JSONException e) {
                    // handling error case.
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Fail to get Data.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // handling error message.
                Toast.makeText(MainActivity.this, " Volley error .." + error, Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void loadJson1(String id) {

        showSimpleProgressDialog(this, "Loading...", "Fetching Json", false);

        String url = "https://vpic.nhtsa.dot.gov/api/vehicles/GetModelsForMakeId/" + id + "?format=json";
        RequestQueue queue1 = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray dataArray = response.getJSONArray("Results");
                    for (int i = 0; i < dataArray.length(); i++) {

                        JSONObject dataObj = dataArray.getJSONObject(i);
                        String make_id = dataObj.getString("Make_ID");
                        String make_name = dataObj.getString("Make_Name");

                        Model model = new Model();
                        model.setCarId(make_id);
                        model.setCarName(make_name);

                        modelArrayList_temp.add(model);
//                        Toast.makeText(MainActivity.this, ""+make_id+" "+make_name, Toast.LENGTH_SHORT).show();
                    }

                    for (int i = 0; i < modelArrayList_temp.size(); i++) {
                        names_temp.add(modelArrayList_temp.get(i).getCarName() + " " + modelArrayList_temp.get(i).getCarId());
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, names_temp);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner2.setAdapter(spinnerArrayAdapter);
                    spinner2.setOnItemSelectedListener(MainActivity.this);

                    removeSimpleProgressDialog();
                } catch (JSONException e) {
                    // handling error case.
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Fail to get Data.." + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // handling error message.
                Toast.makeText(MainActivity.this, " Volley error .." + error, Toast.LENGTH_SHORT).show();
            }
        });

        queue1.add(jsonObjectRequest);
    }

    public static void showSimpleProgressDialog(Context context, String title,
                                                String msg, boolean isCancelable) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(context, title, msg);
                mProgressDialog.setCancelable(isCancelable);
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }

        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeSimpleProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();

        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int position = i - 1;
        int listerner = adapterView.getId();
        switch (listerner) {
            case R.id.spinner1:
                if (i == 0) {
                    car_make = null;
                    ((TextView) view).setTextColor(ContextCompat.getColor(MainActivity.this, androidx.cardview.R.color.cardview_shadow_start_color));
                } else {
                    String id = modelArrayList.get(position).getCarId();

                    ((TextView) view).setTextColor(ContextCompat.getColor(MainActivity.this, com.google.android.material.R.color.primary_dark_material_light));
                    car_make = modelArrayList.get(position).getCarName();
                    modelArrayList_temp.clear();
                    names_temp.clear();
                    names_temp.add(0, "Select the Car Model");

                    loadJson1(id);
                }
                break;

            case R.id.spinner2:
                if (i == 0) {
                    car_model = null;
                    ((TextView) view).setTextColor(ContextCompat.getColor(MainActivity.this, androidx.cardview.R.color.cardview_shadow_start_color));
                } else {

                    ((TextView) view).setTextColor(ContextCompat.getColor(MainActivity.this, com.google.android.material.R.color.primary_dark_material_light));
                    car_model = modelArrayList_temp.get(position).getCarId();
                }
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    void saveCar() {
        Car car_saving = new Car(car_make, car_model, car_image);
        carViewModel.insert(car_saving);
        Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show();
    }

    public void saveImage(Car car) {
        carViewModel.updateCar(car);
        Toast.makeText(this, "Image Saved.", Toast.LENGTH_SHORT).show();
    }


    public void imgSaving(Bitmap imageSources) {
        String c_name = carUpdateImg.getCar_make();
        String c_model = carUpdateImg.getCar_model();
        int c_id = carUpdateImg.getId();
        if ((c_name != null) && (c_model != null) && (c_id != -1) && (imageSources != null)) {

            String c_image = BitMapToString(imageSources);

            if (!c_image.isEmpty()) {
                Car c = new Car(c_name, c_model, c_image);
                c.setId(c_id);
                saveImage(c);
            } else {
                Toast.makeText(MainActivity.this, "Please select the Image", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(MainActivity.this, "Cant store image.", Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("IntentReset")
    public void getImg() {

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, 1);


        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {

            Uri extras = data.getData();

            Bitmap imageSources = null;
            ContentResolver contentResolver = getContentResolver();
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    imageSources = MediaStore.Images.Media.getBitmap(contentResolver, extras);
                    if (imageSources != null) {
                        imgSaving(imageSources);
                    }
                } else {

                    ImageDecoder.Source source = ImageDecoder.createSource(MainActivity.this.getContentResolver(), extras);
                    imageSources = ImageDecoder.decodeBitmap(source);
                    if (imageSources != null) {
                        imgSaving(imageSources);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }


}