package com.gastofacil.modelo;

public class Proveedor {
    private int idProveedor;
    private String nombre;
    private String telefono;
    private String direccion;

    // Constructor vacío
    public Proveedor() {}

    // Constructor lleno
    public Proveedor(int idProveedor, String nombre, String telefono, String direccion) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // Getters y Setters
    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDirection() { return direccion; } // Nota: cámbialo a getDireccion si prefieres, usemos getDireccion para español estándar:
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}