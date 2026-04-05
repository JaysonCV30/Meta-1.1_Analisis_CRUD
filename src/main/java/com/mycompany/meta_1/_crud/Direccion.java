package com.mycompany.meta_1._crud;

public class Direccion {
    private int id;
    private String direccionCompleta;

    public Direccion() {
    }

    public Direccion(int id, String direccionCompleta) {
        this.id = id;
        this.direccionCompleta = direccionCompleta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDireccionCompleta() {
        return direccionCompleta;
    }

    public void setDireccionCompleta(String direccionCompleta) {
        this.direccionCompleta = direccionCompleta;
    }

    @Override
    public String toString() {
        return direccionCompleta;
    }
}
