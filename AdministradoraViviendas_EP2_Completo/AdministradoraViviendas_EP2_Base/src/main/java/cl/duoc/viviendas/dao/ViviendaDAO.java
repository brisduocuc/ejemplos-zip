package cl.duoc.viviendas.dao;

import cl.duoc.viviendas.model.Vivienda;

import java.io.IOException;
import java.util.List;

/**
 * Interfaz DAO para la persistencia de viviendas en almacenamiento externo.
 */
public interface ViviendaDAO {

    /**
     * Carga todas las viviendas almacenadas.
     *
     * @return lista con departamentos y casas registrados
     * @throws IOException si ocurre un error al leer los archivos
     */
    List<Vivienda> cargarTodas() throws IOException;

    /**
     * Guarda el listado completo de viviendas en el almacenamiento persistente.
     *
     * @param viviendas lista actual de viviendas
     * @throws IOException si ocurre un error al escribir los archivos
     */
    void guardarTodas(List<Vivienda> viviendas) throws IOException;
}
