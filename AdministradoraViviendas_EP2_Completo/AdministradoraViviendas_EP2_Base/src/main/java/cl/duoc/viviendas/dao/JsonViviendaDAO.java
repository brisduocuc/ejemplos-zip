package cl.duoc.viviendas.dao;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del patrón DAO que persiste departamentos y casas en archivos
 * JSON separados en la carpeta raíz del proyecto.
 */
public class JsonViviendaDAO implements ViviendaDAO {

    private static final String ARCHIVO_DEPARTAMENTOS = "departamentos.json";
    private static final String ARCHIVO_CASAS = "casas.json";

    private final Path rutaDepartamentos;
    private final Path rutaCasas;
    private final Gson gson;

    public JsonViviendaDAO() {
        this(Paths.get(ARCHIVO_DEPARTAMENTOS), Paths.get(ARCHIVO_CASAS));
    }

    public JsonViviendaDAO(Path rutaDepartamentos, Path rutaCasas) {
        this.rutaDepartamentos = rutaDepartamentos;
        this.rutaCasas = rutaCasas;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Vivienda> cargarTodas() throws IOException {
        List<Vivienda> resultado = new ArrayList<>();
        resultado.addAll(cargarDepartamentos());
        resultado.addAll(cargarCasas());
        return resultado;
    }

    @Override
    public void guardarTodas(List<Vivienda> viviendas) throws IOException {
        List<Departamento> departamentos = new ArrayList<>();
        List<Casa> casas = new ArrayList<>();

        for (Vivienda vivienda : viviendas) {
            if (vivienda instanceof Departamento depto) {
                departamentos.add(depto);
            } else if (vivienda instanceof Casa casa) {
                casas.add(casa);
            }
        }

        guardarLista(rutaDepartamentos, departamentos);
        guardarLista(rutaCasas, casas);
    }

    private List<Departamento> cargarDepartamentos() throws IOException {
        if (Files.notExists(rutaDepartamentos)) {
            return new ArrayList<>();
        }
        Type tipoLista = new TypeToken<List<Departamento>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(rutaDepartamentos, StandardCharsets.UTF_8)) {
            List<Departamento> lista = gson.fromJson(reader, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (JsonParseException e) {
            throw new IOException("El archivo " + ARCHIVO_DEPARTAMENTOS + " tiene un formato JSON inválido.", e);
        }
    }

    private List<Casa> cargarCasas() throws IOException {
        if (Files.notExists(rutaCasas)) {
            return new ArrayList<>();
        }
        Type tipoLista = new TypeToken<List<Casa>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(rutaCasas, StandardCharsets.UTF_8)) {
            List<Casa> lista = gson.fromJson(reader, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (JsonParseException e) {
            throw new IOException("El archivo " + ARCHIVO_CASAS + " tiene un formato JSON inválido.", e);
        }
    }

    private <T> void guardarLista(Path ruta, List<T> elementos) throws IOException {
        try (Writer writer = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            gson.toJson(elementos, writer);
        }
    }
}
