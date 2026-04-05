package com.mycompany.meta_1._crud;

import java.util.List;

public interface IPersonaRepository {
    // Definimos que se puede hacer, pero no como se hace (Abstracción)
    boolean insertar(Persona p);
    List<Persona> listar();
    boolean actualizar(Persona p);
    boolean eliminar(int id);
}
