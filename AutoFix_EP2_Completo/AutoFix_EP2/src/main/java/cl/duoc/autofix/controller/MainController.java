package cl.duoc.autofix.controller;

import cl.duoc.autofix.dao.VehiculoJsonDAO;
import cl.duoc.autofix.dao.VehiculoRepository;
import cl.duoc.autofix.model.Auto;
import cl.duoc.autofix.model.Furgon;
import cl.duoc.autofix.model.Vehiculo;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

/**
 * Controlador principal de la vista de registro y gestión de vehículos del taller.
 */
public class MainController {

    private static final String TIPO_AUTO = "Auto";
    private static final String TIPO_FURGON = "Furgón";

    private static final String ESTILO_EXITO = "-fx-text-fill: #1b7a2b; -fx-font-weight: bold; -fx-font-size: 13px;";
    private static final String ESTILO_ERROR = "-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 13px;";

    @FXML
    private ComboBox<String> cmbTipo;

    @FXML
    private TextField txtMarca;

    @FXML
    private TextField txtAnio;

    @FXML
    private TextField txtKilometraje;

    @FXML
    private TitledPane paneDatosAuto;

    @FXML
    private TextField txtModelo;

    @FXML
    private CheckBox chkGarantiaVigente;

    @FXML
    private CheckBox chkGarantiaActiva;

    @FXML
    private TitledPane paneDatosFurgon;

    @FXML
    private TextField txtCapacidad;

    @FXML
    private TableView<Vehiculo> tblVehiculos;

    @FXML
    private TableColumn<Vehiculo, String> colTipo;

    @FXML
    private TableColumn<Vehiculo, String> colMarca;

    @FXML
    private TableColumn<Vehiculo, Number> colAnio;

    @FXML
    private TableColumn<Vehiculo, Number> colKilometraje;

    @FXML
    private TableColumn<Vehiculo, String> colCostoServicio;

    @FXML
    private Label lblMensaje;

    private final ObservableList<Vehiculo> listaVehiculos = FXCollections.observableArrayList();
    private final VehiculoRepository vehiculoRepository = new VehiculoJsonDAO();
    private final DecimalFormat formatoMoneda;

    public MainController() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        this.formatoMoneda = new DecimalFormat("$#,##0", simbolos);
    }

    @FXML
    public void initialize() {
        configurarSelectorTipo();
        configurarTabla();
        actualizarEstadoCamposEspecificos(null);
        cargarVehiculosIniciales();
    }

    private void configurarSelectorTipo() {
        cmbTipo.setItems(FXCollections.observableArrayList(TIPO_AUTO, TIPO_FURGON));
        cmbTipo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccione...");
                } else {
                    setText(item);
                }
            }
        });

        cmbTipo.valueProperty().addListener((obs, valorAnterior, valorNuevo) ->
                actualizarEstadoCamposEspecificos(valorNuevo)
        );
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getTipo()));
        colMarca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getMarca()));
        colAnio.setCellValueFactory(dato -> new SimpleIntegerProperty(dato.getValue().getAnioFabricacion()));
        colKilometraje.setCellValueFactory(dato -> new SimpleIntegerProperty(dato.getValue().getKilometraje()));
        colCostoServicio.setCellValueFactory(dato ->
                new SimpleStringProperty(formatoMoneda.format(dato.getValue().calcularCostoServicio()))
        );

        alinearColumnaNumerica(colAnio);
        alinearColumnaNumerica(colKilometraje);
        alinearColumnaTextoDerecha(colCostoServicio);

        tblVehiculos.setItems(listaVehiculos);
        tblVehiculos.setPlaceholder(new Label("No hay vehículos registrados."));
    }

    private void alinearColumnaNumerica(TableColumn<Vehiculo, Number> columna) {
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item.intValue()));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void alinearColumnaTextoDerecha(TableColumn<Vehiculo, String> columna) {
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void actualizarEstadoCamposEspecificos(String tipoSeleccionado) {
        boolean esAuto = TIPO_AUTO.equals(tipoSeleccionado);
        boolean esFurgon = TIPO_FURGON.equals(tipoSeleccionado);

        paneDatosAuto.setDisable(!esAuto);
        paneDatosFurgon.setDisable(!esFurgon);
    }

    private void cargarVehiculosIniciales() {
        try {
            List<Vehiculo> cargados = vehiculoRepository.cargarTodos();
            listaVehiculos.setAll(cargados);
            lblMensaje.setText("");
        } catch (IOException e) {
            mostrarError("Error al leer los archivos de vehículos: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegistrar() {
        String tipo = cmbTipo.getValue();
        if (tipo == null || tipo.isBlank()) {
            mostrarError("Tipo: debe seleccionar un tipo de vehículo (Auto o Furgón).");
            return;
        }

        String marca = txtMarca.getText();
        if (marca == null || marca.trim().isEmpty()) {
            mostrarError("Marca: no puede estar vacía ni contener solo espacios.");
            return;
        }

        int anio;
        String textoAnio = txtAnio.getText() != null ? txtAnio.getText().trim() : "";
        try {
            anio = Integer.parseInt(textoAnio);
            if (anio < 1990 || anio > 2026) {
                mostrarError("Año de fabricación: debe ser un número entero entre 1990 y 2026.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("Año de fabricación: debe ser un número entero entre 1990 y 2026.");
            return;
        }

        int kilometraje;
        String textoKilometraje = txtKilometraje.getText() != null ? txtKilometraje.getText().trim() : "";
        try {
            kilometraje = Integer.parseInt(textoKilometraje);
            if (kilometraje <= 0) {
                mostrarError("Kilometraje: debe ser un número entero mayor que cero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("Kilometraje: debe ser un número entero mayor que cero.");
            return;
        }

        Vehiculo nuevoVehiculo;
        try {
            if (TIPO_AUTO.equals(tipo)) {
                String modelo = txtModelo.getText();
                if (modelo == null || modelo.trim().isEmpty()) {
                    mostrarError("Modelo: no puede estar vacío ni contener solo espacios.");
                    return;
                }
                Auto auto = new Auto(
                        marca.trim(),
                        anio,
                        kilometraje,
                        modelo.trim(),
                        chkGarantiaVigente.isSelected()
                );
                if (chkGarantiaActiva.isSelected()) {
                    auto.activarGarantia();
                }
                nuevoVehiculo = auto;
            } else {
                String textoCapacidad = txtCapacidad.getText() != null
                        ? txtCapacidad.getText().trim().replace(',', '.')
                        : "";
                double capacidad;
                try {
                    capacidad = Double.parseDouble(textoCapacidad);
                    if (Double.isNaN(capacidad) || Double.isInfinite(capacidad) || capacidad <= 0) {
                        mostrarError("Capacidad de carga: debe ser un número mayor que cero.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarError("Capacidad de carga: debe ser un número mayor que cero.");
                    return;
                }
                nuevoVehiculo = new Furgon(marca.trim(), anio, kilometraje, capacidad);
            }
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }

        listaVehiculos.add(nuevoVehiculo);
        try {
            vehiculoRepository.guardarTodos(listaVehiculos);
            mostrarExito(nuevoVehiculo.getMarca() + " (" + nuevoVehiculo.getTipo() + ") registrado correctamente.");
            limpiarFormulario();
        } catch (IOException e) {
            listaVehiculos.remove(nuevoVehiculo);
            mostrarError("Error al guardar los archivos: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminar() {
        Vehiculo seleccionado = tblVehiculos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un vehículo de la tabla para eliminarlo.");
            return;
        }

        int indiceAnterior = listaVehiculos.indexOf(seleccionado);
        listaVehiculos.remove(seleccionado);
        try {
            vehiculoRepository.guardarTodos(listaVehiculos);
            tblVehiculos.getSelectionModel().clearSelection();
            mostrarExito(seleccionado.getMarca() + " (" + seleccionado.getTipo() + ") eliminado correctamente.");
        } catch (IOException e) {
            if (indiceAnterior >= 0 && indiceAnterior <= listaVehiculos.size()) {
                listaVehiculos.add(indiceAnterior, seleccionado);
            } else {
                listaVehiculos.add(seleccionado);
            }
            mostrarError("Error al guardar los cambios en los archivos: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        cmbTipo.setValue(null);
        cmbTipo.getSelectionModel().clearSelection();
        txtMarca.clear();
        txtAnio.clear();
        txtKilometraje.clear();
        txtModelo.clear();
        chkGarantiaVigente.setSelected(false);
        chkGarantiaActiva.setSelected(false);
        txtCapacidad.clear();
        actualizarEstadoCamposEspecificos(null);
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
