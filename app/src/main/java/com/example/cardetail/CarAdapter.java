package com.example.cardetail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.ViewHolder> {

    private OnItemClick listerner;


    private List<Car> cars = new ArrayList<>();

    OnItemClick itemClick;

    public CarAdapter(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view, listerner);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Car current = cars.get(position);
        holder.txt1.setText(current.getCar_make());
        holder.txt2.setText(current.getCar_model());
        try {
            byte[] encodeByte = Base64.decode(current.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            holder.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.getMessage();
        }

    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    public void setCars(List<Car> car) {
        cars = car;
        notifyDataSetChanged();
    }

//    public Car getCarAt(int position)
//    {
//        return cars.get(position);
//    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txt1, txt2;
        Button add_img, delete;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView, OnItemClick listener) {
            super(itemView);
            txt1 = itemView.findViewById(R.id.car_make);
            txt2 = itemView.findViewById(R.id.car_model);
            imageView = itemView.findViewById(R.id.car_image);
            add_img = itemView.findViewById(R.id.add_image);
            delete = itemView.findViewById(R.id.delete_data);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            Car c = cars.get(pos);
                            listener.onClick(c);
                        }
                    }
                }
            });

            add_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = getAdapterPosition();
                    if (listerner != null && position != RecyclerView.NO_POSITION) {
                        listerner.onClickAdd(cars.get(position));
                    }
                }
            });
        }

    }


    // Delete button
    public void setOnItemClickListener(OnItemClick itemClickListener) {
        listerner = itemClickListener;
    }

}
