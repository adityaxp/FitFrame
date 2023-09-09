package com.aditya.fitframe;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ReportDataAdapter extends RecyclerView.Adapter<ReportDataAdapter.ReportDataViewHolder> {

    ArrayList<ReportData> reportDataArrayList;
    Context context;
    int lastPosition = -1;

    public ReportDataAdapter(ArrayList<ReportData> reportDataArrayList, Context context) {
        this.reportDataArrayList = reportDataArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReportDataAdapter.ReportDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_data_row_item, parent, false);
        return new ReportDataAdapter.ReportDataViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportDataAdapter.ReportDataViewHolder holder, int position) {

        List<Float> heightData = reportDataArrayList.get(position).getHeightPoints();
        holder.dateInfo.setText(reportDataArrayList.get(position).getDate());

        final int pos = position;
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HeightDataActivity.class);
                intent.putExtra("FullHeight", heightData.get(0));
                intent.putExtra("LeftKneeToAnkleDistance", heightData.get(1));
                intent.putExtra("RightKneeToAnkleDistance", heightData.get(2));
                intent.putExtra("RightHipToKneeDistance", heightData.get(3));
                intent.putExtra("LeftHipToKneeDistance", heightData.get(4));
                intent.putExtra("ShoulderToHipDistance", heightData.get(5));
                intent.putExtra("RSToLS",  heightData.get(6));
                intent.putExtra("ImageURL", reportDataArrayList.get(pos).getImageURL());
                context.startActivity(intent);
            }
        });
        setAnimation(holder.itemView, position);

    }
    public void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(1500);
            view.startAnimation(scaleAnimation);
            lastPosition = position;
        }
    }
    @Override
    public int getItemCount() {
        return reportDataArrayList.size();
    }

    public class ReportDataViewHolder extends RecyclerView.ViewHolder{

        TextView dateInfo;
        CardView cardView;
        public ReportDataViewHolder(@NonNull View itemView) {
            super(itemView);

            dateInfo = itemView.findViewById(R.id.dateTextView);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}

