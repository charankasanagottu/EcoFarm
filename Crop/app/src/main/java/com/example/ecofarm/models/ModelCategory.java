package com.example.ecofarm.models;

public class ModelCategory {
    //make sure to use same name coventions as in firebase
     String id,crop,Benchmark,uid;
     long timestamp;

     //constructor empty required for firebase
    public ModelCategory() {

    }

    //parameterized constructor
    public ModelCategory(String id, String crop, String uid,long timestamp) {
        this.id = id;
        this.Benchmark=Benchmark;
        this.crop=crop;
        this.timestamp=timestamp;
        this.uid=uid;
    }

    public ModelCategory(String id) {
        this.id = id;
    }
    //getters and setters for varaibles


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        crop = crop;
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
