package com.gastofacil.dao;

import com.gastofacil.conexion.ConexionBD;
import com.gastofacil.modelo.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public boolean insertar(Proveedor prov) {
        String sql = "INSERT INTO proveedor (nombre, telefono, direccion) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, prov.getNombre());
            ps.setString(2, prov.getTelefono());
            ps.setString(3, prov.getDireccion());
            
            int filasAffected = ps.executeUpdate();
            return filasAffected > 0;
        } catch (Exception e) {
            System.out.println("CRÍTICO - Error al insertar en ProveedorDAO: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Proveedor prov = new Proveedor();
                prov.setIdProveedor(rs.getInt("idProveedor"));
                prov.setNombre(rs.getString("nombre"));
                prov.setTelefono(rs.getString("telefono"));
                prov.setDireccion(rs.getString("direccion"));
                lista.add(prov);
            }
        } catch (Exception e) {
            System.out.println("CRÍTICO - Error al listar en ProveedorDAO: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // 🗑️ NUEVA FUNCIÓN: Eliminar proveedor por ID
    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE idProveedor = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            int filasAffected = ps.executeUpdate();
            return filasAffected > 0;
        } catch (Exception e) {
            System.out.println("CRÍTICO - Error al eliminar en ProveedorDAO: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ✏️ NUEVA FUNCIÓN: Buscar un solo proveedor (para cargar el formulario de edición)
    public Proveedor buscarPorId(int id) {
        String sql = "SELECT * FROM proveedor WHERE idProveedor = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Proveedor prov = new Proveedor();
                    prov.setIdProveedor(rs.getInt("idProveedor"));
                    prov.setNombre(rs.getString("nombre"));
                    prov.setTelefono(rs.getString("telefono"));
                    prov.setDireccion(rs.getString("direccion"));
                    return prov;
                }
            }
        } catch (Exception e) {
            System.out.println("CRÍTICO - Error al buscar por ID en ProveedorDAO: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 🔄 NUEVA FUNCIÓN: Actualizar los datos de un proveedor existente
    public boolean actualizar(Proveedor prov) {
        String sql = "UPDATE proveedor SET nombre = ?, telefono = ?, direccion = ? WHERE idProveedor = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, prov.getNombre());
            ps.setString(2, prov.getTelefono());
            ps.setString(3, prov.getDireccion());
            ps.setInt(4, prov.getIdProveedor());
            
            int filasAffected = ps.executeUpdate();
            return filasAffected > 0;
        } catch (Exception e) {
            System.out.println("CRÍTICO - Error al actualizar en ProveedorDAO: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}