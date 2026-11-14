package cl.duoc.tallerbicicletas.controller;

import cl.duoc.tallerbicicletas.dao.BicicletaDAO;
import cl.duoc.tallerbicicletas.dao.JsonBicicletaDAO;
import cl.duoc.tallerbicicletas.model.Bicicleta;
import cl.duoc.tallerbicicletas.model.BicicletaElectrica;
import cl.duoc.tallerbicicletas.model.BicicletaMontanya;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

/**
 * Controlador principal de la vista del taller de bicicletas.
 * Gestiona la interacción del formulario, la tabla de registros y la persistencia.
 */
public class MainController {

    private static final String TIPO_ELECTRICA = "Bicicleta Eléctrica";
    private static final String TIPO_MONTANYA = "Bicicleta de Montaña";

    private static final String ESTILO_EXITO = "-fx-text-fill: #1b7a2b; -fx-font-weight: bold; -fx-font-size: 13px;";
    private static final String ESTILO_ERROR = "-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 13px;";

    @FXML
    private ComboBox<String> cmbTipo;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtAnio;

    @FXML
    private TextField txtPeso;

    @FXML
    private TitledPane paneElectrica;

    @FXML
    private TextField txtAutonomia;

    @FXML
    private CheckBox chkBateriaCertificada;

    @FXML
    private CheckBox chkGarantiaExtendida;

    @FXML
    private TitledPane paneMontanya;

    @FXML
    private TextField txtSuspensiones;

    @FXML
    private TableView<Bicicleta> tblBicicletas;

    @FXML
    private TableColumn<Bicicleta, String> colTipo;

    @FXML
    private TableColumn<Bicicleta, String> colCodigo;

    @FXML
    private TableColumn<Bicicleta, Integer> colAnio;

    @FXML
    private TableColumn<Bicicleta, Double> colPeso;

    @FXML
    private TableColumn<Bicicleta, String> colCosto;

    @FXML
    private Label lblMensaje;

    private final ObservableList<Bicicleta> listaBicicletas;
    private final BicicletaDAO bicicletaDAO;
    private final DecimalFormat formatoMoneda;

    public MainController() {
        this.listaBicicletas = FXCollections.observableArrayList();
        this.bicicletaDAO = new JsonBicicletaDAO();

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        this.formatoMoneda = new DecimalFormat("#,##0", simbolos);
    }

    @FXML
    public void initialize() {
        configurarSelectorTipo();
        configurarTabla();
        cargarDatosIniciales();
    }

    private void configurarSelectorTipo() {
        cmbTipo.setItems(FXCollections.observableArrayList(TIPO_ELECTRICA, TIPO_MONTANYA));

        cmbTipo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccione...");
                    setStyle("-fx-text-fill: #6b7280;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: -fx-text-base-color;");
                }
            }
        });

        cmbTipo.valueProperty().addListener((obs, anterior, nuevoValor) -> actualizarEstadoPanelesEspecificos(nuevoValor));
        actualizarEstadoPanelesEspecificos(null);
    }

    private void actualizarEstadoPanelesEspecificos(String tipoSeleccionado) {
        if (TIPO_ELECTRICA.equals(tipoSeleccionado)) {
            paneElectrica.setDisable(false);
            paneMontanya.setDisable(true);
        } else if (TIPO_MONTANYA.equals(tipoSeleccionado)) {
            paneElectrica.setDisable(true);
            paneMontanya.setDisable(false);
        } else {
            paneElectrica.setDisable(true);
            paneMontanya.setDisable(true);
        }
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getTipo()));
        colCodigo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getCodigo()));
        colAnio.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getAnioFabricacion()));
        colPeso.setCellValueFactory(dato -> new SimpleObjectProperty<>(dato.getValue().getPeso()));
        colCosto.setCellValueFactory(dato -> new SimpleStringProperty(
                formatearCosto(dato.getValue().calcularCostoMantencion())
        ));

        tblBicicletas.setItems(listaBicicletas);
    }

    private void cargarDatosIniciales() {
        try {
            List<Bicicleta> cargadas = bicicletaDAO.cargarTodas();
            listaBicicletas.setAll(cargadas);
        } catch (IOException e) {
            mostrarError("Error al leer los archivos de almacenamiento: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegistrar() {
        String tipo = cmbTipo.getValue();
        if (tipo == null || tipo.trim().isEmpty()) {
            mostrarError("Tipo: debe seleccionar un tipo de bicicleta.");
            return;
        }

        String codigo = txtCodigo.getText();
        if (codigo == null || codigo.trim().isEmpty()) {
            mostrarError("Código: no puede estar vacío ni contener solo espacios.");
            return;
        }
        codigo = codigo.trim();

        int anio;
        String textoAnio = txtAnio.getText() != null ? txtAnio.getText().trim() : "";
        try {
            anio = Integer.parseInt(textoAnio);
        } catch (NumberFormatException e) {
            mostrarError("Año de fabricación: debe ser un número entero entre 2000 y 2026.");
            return;
        }
        if (anio < 2000 || anio > 2026) {
            mostrarError("Año de fabricación: debe ser un número entero entre 2000 y 2026.");
            return;
        }

        double peso;
        String textoPeso = txtPeso.getText() != null ? txtPeso.getText().trim().replace(',', '.') : "";
        try {
            peso = Double.parseDouble(textoPeso);
        } catch (NumberFormatException e) {
            mostrarError("Peso (kg): debe ser un número mayor que cero.");
            return;
        }
        if (Double.isNaN(peso) || Double.isInfinite(peso) || peso <= 0) {
            mostrarError("Peso (kg): debe ser un número mayor que cero.");
            return;
        }

        Bicicleta nuevaBicicleta;
        try {
            if (TIPO_ELECTRICA.equals(tipo)) {
                int autonomia;
                String textoAutonomia = txtAutonomia.getText() != null ? txtAutonomia.getText().trim() : "";
                try {
                    autonomia = Integer.parseInt(textoAutonomia);
                } catch (NumberFormatException e) {
                    mostrarError("Autonomía (km): debe ser un número entero mayor que cero.");
                    return;
                }
                if (autonomia <= 0) {
                    mostrarError("Autonomía (km): debe ser un número entero mayor que cero.");
                    return;
                }

                boolean bateriaCertificada = chkBateriaCertificada.isSelected();
                boolean garantiaExtendida = chkGarantiaExtendida.isSelected();

                BicicletaElectrica electrica = new BicicletaElectrica(
                        codigo, anio, peso, autonomia, bateriaCertificada
                );
                if (garantiaExtendida) {
                    electrica.activarGarantiaExtendida();
                }
                nuevaBicicleta = electrica;

            } else if (TIPO_MONTANYA.equals(tipo)) {
                int suspensiones;
                String textoSuspensiones = txtSuspensiones.getText() != null ? txtSuspensiones.getText().trim() : "";
                try {
                    suspensiones = Integer.parseInt(textoSuspensiones);
                } catch (NumberFormatException e) {
                    mostrarError("Suspensiones: debe ser un número entero mayor o igual que cero.");
                    return;
                }
                if (suspensiones < 0) {
                    mostrarError("Suspensiones: debe ser un número entero mayor o igual que cero.");
                    return;
                }

                nuevaBicicleta = new BicicletaMontanya(codigo, anio, peso, suspensiones);
            } else {
                mostrarError("Tipo: debe seleccionar un tipo de bicicleta válido.");
                return;
            }
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }

        listaBicicletas.add(nuevaBicicleta);
        try {
            bicicletaDAO.guardarTodas(listaBicicletas);
        } catch (IOException e) {
            listaBicicletas.remove(nuevaBicicleta);
            mostrarError("Error al guardar los datos en archivo: " + e.getMessage());
            return;
        }

        limpiarFormulario();
        mostrarExito(nuevaBicicleta.getCodigo() + " (" + nuevaBicicleta.getTipo() + ") registrada correctamente.");
    }

    @FXML
    private void handleEliminar() {
        Bicicleta seleccionada = tblBicicletas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una bicicleta de la tabla para eliminarla.");
            return;
        }

        int indiceAnterior = listaBicicletas.indexOf(seleccionada);
        listaBicicletas.remove(seleccionada);

        try {
            bicicletaDAO.guardarTodas(listaBicicletas);
        } catch (IOException e) {
            listaBicicletas.add(indiceAnterior, seleccionada);
            mostrarError("Error al actualizar los archivos tras eliminar: " + e.getMessage());
            return;
        }

        tblBicicletas.getSelectionModel().clearSelection();
        mostrarExito(seleccionada.getCodigo() + " (" + seleccionada.getTipo() + ") eliminada correctamente.");
    }

    private void limpiarFormulario() {
        cmbTipo.setValue(null);
        txtCodigo.clear();
        txtAnio.clear();
        txtPeso.clear();
        txtAutonomia.clear();
        chkBateriaCertificada.setSelected(false);
        chkGarantiaExtendida.setSelected(false);
        txtSuspensiones.clear();
        actualizarEstadoPanelesEspecificos(null);
    }

    private String formatearCosto(double costo) {
        return "$" + formatoMoneda.format(Math.round(costo));
    }

    private void mostrarExito(String mensaje) {
        lblMensaje.setStyle(ESTILO_EXITO);
        lblMensaje.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setStyle(ESTILO_ERROR);
        lblMensaje.setText(mensaje);
    }
}
