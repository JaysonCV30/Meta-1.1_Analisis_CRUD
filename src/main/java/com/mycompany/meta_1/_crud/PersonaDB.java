package com.mycompany.meta_1._crud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDB {
    public boolean insertar(Persona p) {
        String sqlPersona = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";
        String sqlTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        // El try-with-resources cierra las conexiones automáticamente
        try (Connection conn = Conexion.getConnection();
             // RETURN_GENERATED_KEYS nos permite recuperar el ID que MariaDB le asignó
             PreparedStatement pstmtPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {

            // Insertar datos de la persona en los signos de interrogación (?)
            pstmtPersona.setString(1, p.getNombre());
            pstmtPersona.setString(2, p.getDireccion());
            pstmtPersona.executeUpdate();

            // Recuperar el ID generado para esa persona
            try (ResultSet rs = pstmtPersona.getGeneratedKeys()) {
                if (rs.next()) {
                    int personaId = rs.getInt(1); // Obtenemos el ID
                    p.setId(personaId); // Se lo asignamos al objeto Java

                    // Ahora insertamos cada uno de sus teléfonos
                    try (PreparedStatement pstmtTel = conn.prepareStatement(sqlTelefono)) {
                        for (String telefono : p.getTelefonos()) {
                            pstmtTel.setInt(1, personaId);
                            pstmtTel.setString(2, telefono);
                            pstmtTel.executeUpdate();
                        }
                    }
                }
            }
            return true; // Éxito
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Falló
        }
    }

    // Consultar todas las personas
    public List<Persona> listar() {
        List<Persona> lista = new ArrayList<>();
        String sqlPersonas = "SELECT * FROM Personas";
        String sqlTelefonos = "SELECT telefono FROM Telefonos WHERE personaId = ?";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rsPersonas = stmt.executeQuery(sqlPersonas);
             PreparedStatement pstmtTel = conn.prepareStatement(sqlTelefonos)) {

            while (rsPersonas.next()) {
                // Armar el objeto Persona
                Persona p = new Persona();
                p.setId(rsPersonas.getInt("id"));
                p.setNombre(rsPersonas.getString("nombre"));
                p.setDireccion(rsPersonas.getString("direccion"));

                // Buscar y agregar los teléfonos de esta persona
                pstmtTel.setInt(1, p.getId());
                try (ResultSet rsTel = pstmtTel.executeQuery()) {
                    while (rsTel.next()) {
                        p.addTelefono(rsTel.getString("telefono"));
                    }
                }
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Modificar Persona y Teléfonos
    public boolean actualizar(Persona p) {
        String sqlPersona = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";
        // Borrar los viejos e insertar la nueva lista
        String sqlBorrarTels = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlInsertarTels = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = Conexion.getConnection()) {
            
            // Actualizar Nombre y Dirección
            try (PreparedStatement pstmtPersona = conn.prepareStatement(sqlPersona)) {
                pstmtPersona.setString(1, p.getNombre());
                pstmtPersona.setString(2, p.getDireccion());
                pstmtPersona.setInt(3, p.getId());
                pstmtPersona.executeUpdate();
            }

            // Borrar teléfonos antiguos
            try (PreparedStatement pstmtBorrar = conn.prepareStatement(sqlBorrarTels)) {
                pstmtBorrar.setInt(1, p.getId());
                pstmtBorrar.executeUpdate();
            }

            // Insertar la lista de teléfonos actualizada
            try (PreparedStatement pstmtInsertar = conn.prepareStatement(sqlInsertarTels)) {
                for (String telefono : p.getTelefonos()) {
                    pstmtInsertar.setInt(1, p.getId());
                    pstmtInsertar.setString(2, telefono);
                    pstmtInsertar.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Dar de baja
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Personas WHERE id = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();
          
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
