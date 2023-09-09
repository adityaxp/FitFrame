package com.aditya.fitframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CloudSavesActivity extends AppCompatActivity {


    ReportDataAdapter reportDataAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ReportData> cloudSavesDataArrayList;
    ReportData reportData;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_saves);

        cloudSavesDataArrayList = new ArrayList<>();
        RecyclerView cloudSavesRecyclerView = (RecyclerView) findViewById(R.id.cloudSavesRecyclerView);
        backButton = findViewById(R.id.backButton);
        layoutManager = new LinearLayoutManager(this);
        reportDataAdapter = new ReportDataAdapter(cloudSavesDataArrayList, this);
        cloudSavesRecyclerView.setLayoutManager(layoutManager);

        cloudSavesRecyclerView.setAdapter(reportDataAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://fitframe-5a029-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("CloudSaves").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cloudSavesDataArrayList.clear();
                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                     reportData = itemSnapshot.getValue(ReportData.class);
                    cloudSavesDataArrayList.add(reportData);
                }
                reportDataAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}