package com.example.clicker;

import android.app.Application;

import com.example.clicker.objectbo.MyObjectBox;

import io.objectbox.BoxStore;


public class ObjectBoxApp extends Application {

    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        boxStore = MyObjectBox.builder().androidContext(ObjectBoxApp.this).build();
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}