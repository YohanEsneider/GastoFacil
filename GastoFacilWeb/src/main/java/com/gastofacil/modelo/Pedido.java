package com.gastofacil.modelo;

import java.sql.Date;

public class Pedido {
    private int idPedido;
    private int idProveedor;
    private Date fechaPedido;
    private String descripcion;
    private double valorTotal;
    private String metodoPago;
    private String estado;
    private String nombreProveedor; // Variable auxiliar

    public Pedido() {}

    public Pedido(int idPedido, int idProveedor, Date fechaPedido, String descripcion, double valorTotal, String metodoPago, String estado) {
        this.idPedido = idPedido;
        this.idProveedor = idProveedor;
        this.fechaPedido = fechaPedido;
        this.descripcion = descripcion;
        this.valorTotal = valorTotal;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public Date getStaticFechaPedido() { return fechaPedido; }
    public Date getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(Date fechaPedido) { this.fechaPedido = fechaPedido; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }
}