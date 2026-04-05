package com.mycompany.meta_1._crud;

import java.util.ArrayList;
import java.util.List;

public class Persona {
    private int id;
    private String nombre;
    private List<String> telefonos;
    private List<Direccion> direcciones; 

    public Persona() {
        this.telefonos = new ArrayList<>();
        this.direcciones = new ArrayList<>();
    }

    public Persona(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.telefonos = new ArrayList<>();
        this.direcciones = new ArrayList<>(); 
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(List<String> telefonos) {
        this.telefonos = telefonos;
    }
    
    public void addTelefono(String telefono) {
        this.telefonos.add(telefono);
    }

    public List<Direccion> getDirecciones() {
        return direcciones;
    }

    public void setDirecciones(List<Direccion> direcciones) {
        this.direcciones = direcciones;
    }

    public String getDireccion() {
        if (direcciones == null || direcciones.isEmpty()) {
            return "";
        }
        List<String> textosDirecciones = new ArrayList<>();
        for (Direccion d : direcciones) {
            textosDirecciones.add(d.getDireccionCompleta());
        }
        return String.join(" ; ", textosDirecciones);
    }
    
    public void setDireccion(String direccionTexto) {
        this.direcciones.clear(); // Limpiamos las anteriores
        if (direccionTexto != null && !direccionTexto.trim().isEmpty()) {
            // Si el usuario escribe varias separadas por punto y coma, las dividimos
            String[] separadas = direccionTexto.split(";");
            for (String dir : separadas) {
                Direccion nuevaDir = new Direccion();
                nuevaDir.setDireccionCompleta(dir.trim());
                this.direcciones.add(nuevaDir);
            }
        }
    }
}