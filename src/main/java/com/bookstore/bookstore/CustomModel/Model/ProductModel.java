package com.bookstore.bookstore.CustomModel.Model;

public class ProductModel {

    public  String pid;
    public  String code;
    public  String name;
    public  boolean active;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    public ProductModel(){}

    public ProductModel(String pid, String code, String name, boolean active) {
        this.pid = pid;
        this.code = code;
        this.name = name;
        this.active = active;
    }
}
