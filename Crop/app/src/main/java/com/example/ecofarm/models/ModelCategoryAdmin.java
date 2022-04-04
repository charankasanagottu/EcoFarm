package com.example.ecofarm.models;

public class ModelCategoryAdmin {
    //make sure to use same name coventions as in firebase
    String id,Crop,Benchmark,uid;
    long timestamp;

    //constructor empty required for firebase
    public ModelCategoryAdmin() {

    }

    //parameterized constructor
    public ModelCategoryAdmin(String id) {
        this.id = id;
        this.Benchmark=Benchmark;
        this.Crop=Crop;
        this.timestamp=timestamp;
        this.uid=uid;
    }

    //getters and setters for varaibles


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrop() {
        return Crop;
    }

    public void setCrop(String crop) {
        Crop = crop;
    }

    public String getBenchmark() {
        return Benchmark;
    }

    public void setBenchmark(String benchmark) {
        Benchmark = benchmark;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
