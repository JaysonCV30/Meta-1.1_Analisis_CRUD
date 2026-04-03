package com.mycompany.meta_1._crud;

import java.util.ArrayList;
import java.util.List;

public class Persona {
    private int id;
    private String nombre;
    private String direccion;
    private List<String> telefonos; // Una persona puede tener N teléfonos

    // Constructor vacío
    public Persona() {
        this.telefonos = new ArrayList<>();
    }

    // Constructor con datos
    public Persona(int id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefonos = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public List<String> getTelefonos() { return telefonos; }
    public void setTelefonos(List<String> telefonos) { this.telefonos = telefonos; }
    
    public void addTelefono(String telefono) {
        this.telefonos.add(telefono);
    }

    @Override
    public String toString() {
        return nombre + " (" + direccion + ")";
    }
}
