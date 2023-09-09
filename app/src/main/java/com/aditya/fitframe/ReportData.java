package com.aditya.fitframe;

import java.util.List;

public class ReportData {


    public List<Float> heightPoints;
    public String date,  imageURL;

    public ReportData() {
    }

    public ReportData(List<Float>heightPoints, String date, String imageURL) {
        this.heightPoints = heightPoints;
        this.date = date;
        this.imageURL = imageURL;
    }

    public List<Float> getHeightPoints() {
        return heightPoints;
    }

    public String getDate() {
        return date;
    }

    public String getImageURL() {
        return imageURL;
    }

}
