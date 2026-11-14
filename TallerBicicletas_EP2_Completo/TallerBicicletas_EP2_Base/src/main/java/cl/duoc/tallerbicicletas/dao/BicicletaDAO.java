package cl.duoc.tallerbicicletas.dao;

import cl.duoc.tallerbicicletas.model.Bicicleta;

import java.io.IOException;
import java.util.List;

/**
 * Contrato de acceso a datos para la persistencia de bicicletas del taller.
 */
public interface BicicletaDAO {

    /**
     * Carga todas las bicicletas almacenadas en los archivos de persistencia.
     *
     * @return lista con todas las bicicletas registradas
     * @throws IOException si ocurre un error al leer los archivos
     */
    List<Bicicleta> cargarTodas() throws IOException;

    /**
     * Guarda la lista completa de bicicletas en los archivos correspondientes según su tipo.
     *
     * @param bicicletas lista actual de bicicletas a persistir
     * @throws IOException si ocurre un error al escribir los archivos
     */
    void guardarTodas(List<Bicicleta> bicicletas) throws IOException;
}
