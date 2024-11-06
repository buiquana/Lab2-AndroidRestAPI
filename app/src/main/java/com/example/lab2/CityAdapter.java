package com.example.lab2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder>{
    private List<Map<String,Object>> cities;
    private CityViewHolder.OnCityActionListener onCityActionListener;
    private List<String> documentIds;

    public CityAdapter(List<Map<String, Object>> cities, CityViewHolder.OnCityActionListener onCityActionListener, List<String> documentIds) {
        this.cities = cities;
        this.onCityActionListener = onCityActionListener;
        this.documentIds = documentIds;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_item,parent,false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        Map<String,Object> city = cities.get(position);
        String docId = documentIds.get(position);
        holder.cityName.setText((String) city.get("name"));
        holder.country.setText((String) city.get("country"));
        holder.population.setText(String.valueOf(city.get("population")));

        holder.btnEdit.setOnClickListener(v -> onCityActionListener.onEditClick(docId)); // Chuyển ID tài liệu khi nhấn sửa
        holder.btnDelete.setOnClickListener(v -> onCityActionListener.onDeleteClick(docId));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static class CityViewHolder extends RecyclerView.ViewHolder{
        TextView cityName,country,population;
        ImageButton btnEdit,btnDelete;

        public CityViewHolder(@NonNull View itemView){
            super(itemView);
            cityName = itemView.findViewById(R.id.cityNameTextView);
            country = itemView.findViewById(R.id.countryTextView);
            population = itemView.findViewById(R.id.populationTextView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public interface OnCityActionListener {
            void onEditClick(String docId);
            void onDeleteClick(String docId);
        }
    }
}
