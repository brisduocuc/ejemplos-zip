package cl.duoc.autofix.dao;

import cl.duoc.autofix.model.Auto;
import cl.duoc.autofix.model.Furgon;
import cl.duoc.autofix.model.Vehiculo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación DAO/Repository que persiste autos y furgones en archivos JSON
 * separados (autos.json y furgones.json) en la carpeta raíz del proyecto.
 */
public class VehiculoJsonDAO implements VehiculoRepository {

    private static final String ARCHIVO_AUTOS = "autos.json";
    private static final String ARCHIVO_FURGONES = "furgones.json";

    private final Path rutaAutos;
    private final Path rutaFurgones;
    private final Gson gson;

    public VehiculoJsonDAO() {
        this(Path.of(ARCHIVO_AUTOS), Path.of(ARCHIVO_FURGONES));
    }

    public VehiculoJsonDAO(Path rutaAutos, Path rutaFurgones) {
        this.rutaAutos = rutaAutos;
        this.rutaFurgones = rutaFurgones;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Vehiculo> cargarTodos() throws IOException {
        List<Vehiculo> vehiculos = new ArrayList<>();
        vehiculos.addAll(cargarLista(rutaAutos, new TypeToken<List<Auto>>() {}.getType(), ARCHIVO_AUTOS));
        vehiculos.addAll(cargarLista(rutaFurgones, new TypeToken<List<Furgon>>() {}.getType(), ARCHIVO_FURGONES));
        return vehiculos;
    }

    @Override
    public void guardarTodos(List<Vehiculo> vehiculos) throws IOException {
        List<Auto> autos = new ArrayList<>();
        List<Furgon> furgones = new ArrayList<>();

        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo instanceof Auto auto) {
                autos.add(auto);
            } else if (vehiculo instanceof Furgon furgon) {
                furgones.add(furgon);
            }
        }

        guardarLista(rutaAutos, autos, ARCHIVO_AUTOS);
        guardarLista(rutaFurgones, furgones, ARCHIVO_FURGONES);
    }

    private <T> List<T> cargarLista(Path ruta, Type tipoLista, String nombreArchivo) throws IOException {
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        try {
            String contenido = Files.readString(ruta, StandardCharsets.UTF_8);
            if (contenido == null || contenido.trim().isEmpty()) {
                return new ArrayList<>();
            }
            List<T> lista = gson.fromJson(contenido, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (JsonParseException e) {
            throw new IOException("El archivo " + nombreArchivo + " tiene un formato JSON inválido.", e);
        } catch (IOException e) {
            throw new IOException("No se pudo leer el archivo " + nombreArchivo + ".", e);
        }
    }

    private <T> void guardarLista(Path ruta, List<T> lista, String nombreArchivo) throws IOException {
        try {
            String json = gson.toJson(lista);
            Files.writeString(ruta, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("No se pudo guardar el archivo " + nombreArchivo + ".", e);
        }
    }
}
