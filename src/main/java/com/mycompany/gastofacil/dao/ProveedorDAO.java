package com.mycompany.gastofacil.dao;

import com.mycompany.gastofacil.conexion.ConexionBD;
import com.mycompany.gastofacil.modelo.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    // 1. Método para INSERTAR un proveedor (CREATE)
    public void insertar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedor (nombre, telefono, direccion) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getTelefono());
            ps.setString(3, proveedor.getDireccion());
            ps.executeUpdate();
            System.out.println("Proveedor guardado correctamente.");
            
        } catch (Exception e) {
            System.out.println("Error al insertar proveedor: " + e.getMessage());
        }
    }

    // 2. Método para CONSULTAR la lista de proveedores (READ)
    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("idproveedor")); // Asegúrate de que coincida con tu columna en MySQL
                p.setNombre(rs.getString("nombre"));
                p.setTelefono(rs.getString("telefono"));
                p.setDireccion(rs.getString("direccion"));
                lista.add(p);
            }
            
        } catch (Exception e) {
            System.out.println("Error al listar proveedores: " + e.getMessage());
        }
        return lista;
    }

    // 3. Método para ACTUALIZAR un proveedor (UPDATE)
    public void actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedor SET nombre = ?, telefono = ?, direccion = ? WHERE idproveedor = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getTelefono());
            ps.setString(3, proveedor.getDireccion());
            ps.setInt(4, proveedor.getIdProveedor());
            ps.executeUpdate();
            System.out.println("Proveedor actualizado correctamente.");
            
        } catch (Exception e) {
            System.out.println("Error al actualizar proveedor: " + e.getMessage());
        }
    }

    // 4. Método para ELIMINAR un proveedor (DELETE)
    public void eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE idproveedor = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Proveedor eliminado correctamente.");
            
        } catch (Exception e) {
            System.out.println("Error al eliminar proveedor: " + e.getMessage());
        }
    }
}