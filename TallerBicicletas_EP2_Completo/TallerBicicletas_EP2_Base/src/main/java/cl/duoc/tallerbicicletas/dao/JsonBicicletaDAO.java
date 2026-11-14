package cl.duoc.tallerbicicletas.dao;

import cl.duoc.tallerbicicletas.model.Bicicleta;
import cl.duoc.tallerbicicletas.model.BicicletaElectrica;
import cl.duoc.tallerbicicletas.model.BicicletaMontanya;
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
 * Implementación del patrón DAO/Repository que persiste las bicicletas
 * en archivos JSON separados en la carpeta raíz del proyecto.
 */
public class JsonBicicletaDAO implements BicicletaDAO {

    private static final String ARCHIVO_ELECTRICAS = "bicicletas_electricas.json";
    private static final String ARCHIVO_MONTANYA = "bicicletas_montanya.json";

    private final Gson gson;

    public JsonBicicletaDAO() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public List<Bicicleta> cargarTodas() throws IOException {
        List<Bicicleta> bicicletas = new ArrayList<>();
        bicicletas.addAll(cargarElectricas());
        bicicletas.addAll(cargarMontanya());
        return bicicletas;
    }

    @Override
    public void guardarTodas(List<Bicicleta> bicicletas) throws IOException {
        List<BicicletaElectrica> electricas = new ArrayList<>();
        List<BicicletaMontanya> montanya = new ArrayList<>();

        for (Bicicleta bicicleta : bicicletas) {
            if (bicicleta instanceof BicicletaElectrica electrica) {
                electricas.add(electrica);
            } else if (bicicleta instanceof BicicletaMontanya biciMontanya) {
                montanya.add(biciMontanya);
            }
        }

        guardarLista(Paths.get(ARCHIVO_ELECTRICAS), electricas);
        guardarLista(Paths.get(ARCHIVO_MONTANYA), montanya);
    }

    private List<BicicletaElectrica> cargarElectricas() throws IOException {
        Path ruta = Paths.get(ARCHIVO_ELECTRICAS);
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        Type tipoLista = new TypeToken<List<BicicletaElectrica>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            List<BicicletaElectrica> lista = gson.fromJson(reader, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (JsonParseException e) {
            throw new IOException("El archivo " + ARCHIVO_ELECTRICAS + " tiene un formato JSON inválido.", e);
        }
    }

    private List<BicicletaMontanya> cargarMontanya() throws IOException {
        Path ruta = Paths.get(ARCHIVO_MONTANYA);
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        Type tipoLista = new TypeToken<List<BicicletaMontanya>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            List<BicicletaMontanya> lista = gson.fromJson(reader, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (JsonParseException e) {
            throw new IOException("El archivo " + ARCHIVO_MONTANYA + " tiene un formato JSON inválido.", e);
        }
    }

    private <T> void guardarLista(Path ruta, List<T> lista) throws IOException {
        try (Writer writer = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            gson.toJson(lista, writer);
        }
    }
}
