package com.mycompany.gastofacil.modelo;

public class Proveedor {
    // Atributos de la tabla proveedor
    private int idProveedor;
    private String nombre;
    private String telefono;
    private String direccion;

    // Constructor vacío (obligatorio para buenas prácticas)
    public Proveedor() {
    }

    // Constructor con todos los atributos
    public Proveedor(int idProveedor, String nombre, String telefono, String direccion) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // Constructor sin ID (nos va a servir para cuando vayamos a INSERTAR uno nuevo)
    public Proveedor(String nombre, String telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // --- GETTERS Y SETTERS ---
    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}