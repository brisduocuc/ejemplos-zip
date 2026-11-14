package cl.duoc.viviendas.controller;

import cl.duoc.viviendas.dao.JsonViviendaDAO;
import cl.duoc.viviendas.dao.ViviendaDAO;
import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controlador principal de la aplicación. Coordina el formulario de registro,
 * las validaciones de entrada, la tabla de viviendas y la persistencia JSON.
 */
public class MainController {

    private static final String TIPO_DEPARTAMENTO = "Departamento";
    private static final String TIPO_CASA = "Casa";

    @FXML
    private ComboBox<String> cmbTipo;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtSuperficie;

    @FXML
    private TextField txtHabitaciones;

    @FXML
    private VBox panelDepartamento;

    @FXML
    private TextField txtPiso;

    @FXML
    private CheckBox chkGastoComunAlDia;

    @FXML
    private CheckBox chkEstacionamiento;

    @FXML
    private VBox panelCasa;

    @FXML
    private CheckBox chkTienePatio;

    @FXML
    private Button btnRegistrar;

    @FXML
    private TableView<Vivienda> tblViviendas;

    @FXML
    private TableColumn<Vivienda, String> colTipo;

    @FXML
    private TableColumn<Vivienda, String> colCodigo;

    @FXML
    private TableColumn<Vivienda, Number> colSuperficie;

    @FXML
    private TableColumn<Vivienda, Number> colHabitaciones;

    @FXML
    private TableColumn<Vivienda, Number> colCostoArriendo;

    @FXML
    private Button btnEliminar;

    @FXML
    private Label lblMensaje;

    private final ObservableList<Vivienda> listaViviendas = FXCollections.observableArrayList();
    private final ViviendaDAO viviendaDAO = new JsonViviendaDAO();
    private final DecimalFormat formatoPesos = crearFormatoPesos();

    @FXML
    public void initialize() {
        configurarSelectorTipo();
        configurarTabla();
        cargarViviendasIniciales();
    }

    private void configurarSelectorTipo() {
        cmbTipo.setItems(FXCollections.observableArrayList(TIPO_DEPARTAMENTO, TIPO_CASA));
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

        cmbTipo.valueProperty().addListener((obs, anterior, seleccionado) -> actualizarPanelesEspecificos(seleccionado));
        actualizarPanelesEspecificos(null);
    }

    private void actualizarPanelesEspecificos(String tipoSeleccionado) {
        if (TIPO_DEPARTAMENTO.equals(tipoSeleccionado)) {
            panelDepartamento.setDisable(false);
            panelCasa.setDisable(true);
            chkTienePatio.setSelected(false);
        } else if (TIPO_CASA.equals(tipoSeleccionado)) {
            panelCasa.setDisable(false);
            panelDepartamento.setDisable(true);
            limpiarCamposDepartamento();
        } else {
            panelDepartamento.setDisable(true);
            panelCasa.setDisable(true);
            limpiarCamposDepartamento();
            chkTienePatio.setSelected(false);
        }
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo()));
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colSuperficie.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSuperficie()));
        colHabitaciones.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getHabitaciones()));
        colCostoArriendo.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().calcularCostoArriendo()));

        colSuperficie.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.US, "%.1f", item.doubleValue()));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        colHabitaciones.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item.intValue()));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        colCostoArriendo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatoPesos.format(Math.round(item.doubleValue())));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        tblViviendas.setItems(listaViviendas);
    }

    private void cargarViviendasIniciales() {
        try {
            List<Vivienda> cargadas = viviendaDAO.cargarTodas();
            listaViviendas.setAll(cargadas);
            limpiarMensaje();
        } catch (IOException e) {
            mostrarError("Error al leer los archivos de viviendas: " + e.getMessage());
        }
    }

    @FXML
    private void onRegistrar() {
        String tipo = cmbTipo.getValue();
        if (tipo == null || tipo.isBlank()) {
            mostrarError("Tipo: debe seleccionar Departamento o Casa.");
            return;
        }

        String codigo = txtCodigo.getText();
        if (codigo == null || codigo.trim().isEmpty()) {
            mostrarError("Código: no puede estar vacío ni contener solo espacios.");
            return;
        }

        double superficie;
        try {
            String textoSuperficie = txtSuperficie.getText() == null ? "" : txtSuperficie.getText().trim().replace(',', '.');
            superficie = Double.parseDouble(textoSuperficie);
            if (Double.isNaN(superficie) || Double.isInfinite(superficie) || superficie < 20 || superficie > 500) {
                mostrarError("Superficie: debe ser un número entre 20 y 500.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("Superficie: debe ser un número entre 20 y 500.");
            return;
        }

        int habitaciones;
        try {
            String textoHabitaciones = txtHabitaciones.getText() == null ? "" : txtHabitaciones.getText().trim();
            habitaciones = Integer.parseInt(textoHabitaciones);
            if (habitaciones <= 0) {
                mostrarError("Habitaciones: debe ser un número entero mayor que cero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("Habitaciones: debe ser un número entero mayor que cero.");
            return;
        }

        Vivienda nuevaVivienda;
        try {
            if (TIPO_DEPARTAMENTO.equals(tipo)) {
                int piso;
                try {
                    String textoPiso = txtPiso.getText() == null ? "" : txtPiso.getText().trim();
                    piso = Integer.parseInt(textoPiso);
                    if (piso <= 0) {
                        mostrarError("Piso: debe ser un número entero mayor que cero.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarError("Piso: debe ser un número entero mayor que cero.");
                    return;
                }

                Departamento depto = new Departamento(
                        codigo.trim(),
                        superficie,
                        habitaciones,
                        piso,
                        chkGastoComunAlDia.isSelected()
                );
                if (chkEstacionamiento.isSelected()) {
                    depto.asignarEstacionamiento();
                }
                nuevaVivienda = depto;
            } else {
                nuevaVivienda = new Casa(
                        codigo.trim(),
                        superficie,
                        habitaciones,
                        chkTienePatio.isSelected()
                );
            }
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }

        List<Vivienda> respaldo = new ArrayList<>(listaViviendas);
        listaViviendas.add(nuevaVivienda);
        try {
            viviendaDAO.guardarTodas(listaViviendas);
            limpiarFormulario();
            mostrarExito(nuevaVivienda.getCodigo() + " (" + nuevaVivienda.getTipo() + ") registrada correctamente.");
        } catch (IOException e) {
            listaViviendas.setAll(respaldo);
            mostrarError("Error al guardar la vivienda en archivo: " + e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Vivienda seleccionada = tblViviendas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una vivienda de la tabla para eliminarla.");
            return;
        }

        List<Vivienda> respaldo = new ArrayList<>(listaViviendas);
        listaViviendas.remove(seleccionada);
        try {
            viviendaDAO.guardarTodas(listaViviendas);
            tblViviendas.getSelectionModel().clearSelection();
            mostrarExito(seleccionada.getCodigo() + " (" + seleccionada.getTipo() + ") eliminada correctamente.");
        } catch (IOException e) {
            listaViviendas.setAll(respaldo);
            mostrarError("Error al actualizar los archivos tras eliminar: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        cmbTipo.getSelectionModel().clearSelection();
        cmbTipo.setValue(null);
        txtCodigo.clear();
        txtSuperficie.clear();
        txtHabitaciones.clear();
        limpiarCamposDepartamento();
        chkTienePatio.setSelected(false);
        actualizarPanelesEspecificos(null);
    }

    private void limpiarCamposDepartamento() {
        txtPiso.clear();
        chkGastoComunAlDia.setSelected(false);
        chkEstacionamiento.setSelected(false);
    }

    private void mostrarExito(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: #1b7a2b; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblMensaje.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblMensaje.setText(mensaje);
    }

    private void limpiarMensaje() {
        lblMensaje.setText("");
    }

    private DecimalFormat crearFormatoPesos() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-CL"));
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("$#,##0", simbolos);
    }
}
