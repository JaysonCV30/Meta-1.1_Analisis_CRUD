package com.mycompany.meta_1._crud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDB implements IPersonaRepository {

    // 1. LISTAR TODAS LAS PERSONAS
    public List<Persona> listar() {
        List<Persona> lista = new ArrayList<>();
        String sql = "SELECT * FROM Personas";
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Persona p = new Persona(id, rs.getString("nombre"));
                
                // Cargamos sus datos asociados (Abstracción)
                p.setTelefonos(buscarTelefonos(id, con));
                p.setDirecciones(buscarDirecciones(id, con));
                
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2. INSERTAR NUEVA PERSONA
    public boolean insertar(Persona p) {
        // Ya no insertamos la dirección aquí, solo el nombre
        String sqlPersona = "INSERT INTO Personas (nombre) VALUES (?)";
        Connection con = null;
        
        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false); // Iniciamos transacción

            PreparedStatement psP = con.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS);
            psP.setString(1, p.getNombre());
            psP.executeUpdate();
            
            ResultSet rs = psP.getGeneratedKeys();
            if (rs.next()) {
                p.setId(rs.getInt(1));
                
                // Guardamos los datos de las otras tablas
                guardarTelefonos(p, con);
                vincularDirecciones(p, con);
            }

            con.commit(); // Todo salió bien, guardamos
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    // 3. ACTUALIZAR PERSONA EXISTENTE
    public boolean actualizar(Persona p) {
        String sql = "UPDATE Personas SET nombre = ? WHERE id = ?";
        Connection con = null;
        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getNombre());
                ps.setInt(2, p.getId());
                ps.executeUpdate();
            }
            
            // Actualizamos las listas en sus respectivas tablas
            guardarTelefonos(p, con);
            vincularDirecciones(p, con);
            
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    // 4. ELIMINAR PERSONA
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Personas WHERE id = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MÉTODOS PRIVADOS DE LÓGICA INTERNA (Encapsulamiento) ---

    private List<String> buscarTelefonos(int idPersona, Connection con) throws SQLException {
        List<String> tels = new ArrayList<>();
        String sql = "SELECT telefono FROM Telefonos WHERE personaId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tels.add(rs.getString("telefono"));
        }
        return tels;
    }

    private List<Direccion> buscarDirecciones(int idPersona, Connection con) throws SQLException {
        List<Direccion> dirs = new ArrayList<>();
        // JOIN para sacar las direcciones que le pertenecen a esta persona
        String sql = "SELECT d.id_direccion, d.direccion_completa FROM Direcciones d " +
                     "JOIN Persona_Direccion pd ON d.id_direccion = pd.id_direccion " +
                     "WHERE pd.id_persona = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dirs.add(new Direccion(rs.getInt("id_direccion"), rs.getString("direccion_completa")));
            }
        }
        return dirs;
    }

    private void vincularDirecciones(Persona p, Connection con) throws SQLException {
        // Borrar vínculos viejos
        String sqlDel = "DELETE FROM Persona_Direccion WHERE id_persona = ?";
        try (PreparedStatement psDel = con.prepareStatement(sqlDel)) {
            psDel.setInt(1, p.getId());
            psDel.executeUpdate();
        }

        // Insertar los nuevos
        for (Direccion d : p.getDirecciones()) {
            int idDir;
            // Buscar si la dirección ya existe en el catálogo para no duplicarla
            String sqlCheck = "SELECT id_direccion FROM Direcciones WHERE direccion_completa = ?";
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setString(1, d.getDireccionCompleta());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    idDir = rs.getInt(1); // Ya existe, usamos su ID
                } else {
                    // Es nueva, la agregamos al catálogo
                    String sqlInsDir = "INSERT INTO Direcciones (direccion_completa) VALUES (?)";
                    try (PreparedStatement psIns = con.prepareStatement(sqlInsDir, Statement.RETURN_GENERATED_KEYS)) {
                        psIns.setString(1, d.getDireccionCompleta());
                        psIns.executeUpdate();
                        ResultSet rsK = psIns.getGeneratedKeys();
                        rsK.next();
                        idDir = rsK.getInt(1);
                    }
                }
            }
            // Vincular la persona con la dirección
            String sqlLink = "INSERT INTO Persona_Direccion (id_persona, id_direccion) VALUES (?, ?)";
            try (PreparedStatement psLink = con.prepareStatement(sqlLink)) {
                psLink.setInt(1, p.getId());
                psLink.setInt(2, idDir);
                psLink.executeUpdate();
            }
        }
    }

    private void guardarTelefonos(Persona p, Connection con) throws SQLException {
        // Borramos los anteriores
        String sqlDel = "DELETE FROM Telefonos WHERE personaId = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlDel)) {
            ps.setInt(1, p.getId());
            ps.executeUpdate();
        }
        // Insertamos los de la lista actual
        String sqlIns = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
        try (PreparedStatement psIns = con.prepareStatement(sqlIns)) {
            for (String tel : p.getTelefonos()) {
                psIns.setInt(1, p.getId());
                psIns.setString(2, tel);
                psIns.executeUpdate(); 
            }
        }
    }
}