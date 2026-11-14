package cl.duoc.autofix.dao;

import cl.duoc.autofix.model.Vehiculo;

import java.io.IOException;
import java.util.List;

/**
 * Contrato de acceso a datos para la persistencia de vehículos del taller.
 */
public interface VehiculoRepository {

    /**
     * Carga todos los vehículos almacenados en los archivos de persistencia.
     *
     * @return lista con los vehículos registrados
     * @throws IOException si ocurre un error al leer los archivos
     */
    List<Vehiculo> cargarTodos() throws IOException;

    /**
     * Guarda la lista completa de vehículos en los archivos de persistencia correspondientes.
     *
     * @param vehiculos lista actual de vehículos a persistir
     * @throws IOException si ocurre un error al escribir los archivos
     */
    void guardarTodos(List<Vehiculo> vehiculos) throws IOException;
}
