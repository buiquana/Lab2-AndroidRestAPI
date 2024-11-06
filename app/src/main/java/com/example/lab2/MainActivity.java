package com.example.lab2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CityAdapter.CityViewHolder.OnCityActionListener {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CityAdapter adapter;
    private List<Map<String,Object>> cityList;
    private List<String> documentIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        cityList = new ArrayList<>();
        documentIds = new ArrayList<>();

        adapter = new CityAdapter(cityList,this,documentIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db.collection("cities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                addSampleDataToFirestore();
            }
        });

        loadCitiesFromFirestore();

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> showAddCityDialog());

    }

    @Override
    public void onEditClick(String docId){
        db.collection("cities").document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        Map<String,Object> city = documentSnapshot.getData();
                        showEditCityDialog(city,docId);
                    }else {
                        Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error: ", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeleteClick(String docId){
        db.collection("cities").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "City deleted", Toast.LENGTH_SHORT).show();
                    loadCitiesFromFirestore();
                }).addOnFailureListener(e -> Toast.makeText(this, "Error: ", Toast.LENGTH_SHORT).show());
    }

    private void showEditCityDialog(Map<String,Object> city,String docId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_city,null);
        builder.setView(dialogView);

        EditText edtName = dialogView.findViewById(R.id.editTextName);
        EditText edtCountry = dialogView.findViewById(R.id.editTextCountry);
        EditText edtPopulation = dialogView.findViewById(R.id.editTextPopulation);

        edtName.setText((String) city.get("name"));
        edtCountry.setText((String) city.get("country"));
        edtPopulation.setText(String.valueOf(city.get("population")));

        builder.setTitle("Edit City").setPositiveButton("Save",(dialog, which) -> {
            String name = edtName.getText().toString();
            String country = edtCountry.getText().toString();
            String populationText = edtPopulation.getText().toString();

            if (!name.isEmpty() && !country.isEmpty() && !populationText.isEmpty()){
                int population = Integer.parseInt(populationText);
                city.put("name",name);
                city.put("country",country);
                city.put("population",population);

                db.collection("cities").document(docId)
                        .set(city)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "City updated", Toast.LENGTH_SHORT).show();
                            loadCitiesFromFirestore();
                        }).addOnFailureListener(e -> Toast.makeText(this, "Error: ", Toast.LENGTH_SHORT).show());
            }else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel",(dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateCityInFirestore(Map<String,Object> city,int position){
        String cityId = (String) city.get("id");
        db.collection("cities").document(cityId)
                .set(city)
                .addOnSuccessListener(aVoid ->{
                    cityList.set(position,city);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "City updaed", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(this, "Error: ", Toast.LENGTH_SHORT).show());
    }

    private void deleteCityFromFirestore(Map<String,Object> city, int position){
        String cityId = (String) city.get("id");
        db.collection("cities").document(cityId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    cityList.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "City deleted", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(this, "Error: ", Toast.LENGTH_SHORT).show());
    }

    private void showAddCityDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_city,null);
        builder.setView(dialogView);

        EditText edtName = dialogView.findViewById(R.id.editTextName);
        EditText edtCountry = dialogView.findViewById(R.id.editTextCountry);
        EditText edtPopulation = dialogView.findViewById(R.id.editTextPopulation);

        builder.setTitle("Add City").setPositiveButton("Add", (dialog, which) -> {
            String name = edtName.getText().toString();
            String country = edtCountry.getText().toString();
            String populationText = edtPopulation.getText().toString();

            if (!name.isEmpty() && !country.isEmpty() && !populationText.isEmpty()){
                int population = Integer.parseInt(populationText);
                City city = new City(name,country,population);
                addCityToFirestore(city);
            }else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel",(dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addCityToFirestore(City city){
        db.collection("cities")
                .add(city.toMap())
                .addOnSuccessListener(documentReference -> {
                    cityList.add(city.toMap());
                    documentIds.add(documentReference.getId());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "City added", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(this, "Error: ", Toast.LENGTH_SHORT).show());
    }

    private void loadCitiesFromFirestore(){
        db.collection("cities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cityList.clear();
                    documentIds.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                        Map<String,Object> city = document.getData();
                        cityList.add(city);
                        documentIds.add(document.getId());
                    }
                    adapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> Toast.makeText(this, "Error: ", Toast.LENGTH_SHORT).show());
    }

    private void addSampleDataToFirestore(){
        Map<String,Object> city1 = new HashMap<>();
        city1.put("name","London");
        city1.put("country","England");
        city1.put("population", 1500000);

        Map<String,Object> city2 = new HashMap<>();
        city2.put("name","Barcelona");
        city2.put("country","Spain");
        city2.put("population", 850000);

        Map<String,Object> city3 = new HashMap<>();
        city3.put("name","Paris");
        city3.put("country","France");
        city3.put("population", 700000);

        db.collection("cities").add(city1);
        db.collection("cities").add(city2);
        db.collection("cities").add(city3);
    }

}