package com.mycompany.meta_1._crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PersonaControllerGUI {
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtBuscar;
    @FXML private TextArea txtTelefonos; 
    
    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, Integer> colId;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colDireccion;

    // --- VARIABLES DE LÓGICA ---
    private IPersonaRepository personaDB = new PersonaDB();
    private ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();
    private Persona personaSeleccionada = null; 

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        // La interfaz gráfica se encarga de cómo se muestran los datos en la tabla
        colDireccion.setCellValueFactory(cellData -> {
            List<Direccion> dirs = cellData.getValue().getDirecciones();
            String dirStr = dirs.stream()
                                .map(Direccion::getDireccionCompleta)
                                .collect(Collectors.joining(" ; "));
            return new SimpleStringProperty(dirStr);
        });

        // Cargar los datos iniciales
        cargarTabla();

        // --- LÓGICA DEL BUSCADOR ---
        FilteredList<Persona> datosFiltrados = new FilteredList<>(listaPersonas, p -> true);
        
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            datosFiltrados.setPredicate(persona -> {
                if (newValue == null || newValue.isEmpty()) return true;
                
                String textoBuscado = newValue.toLowerCase();
                if (persona.getNombre().toLowerCase().contains(textoBuscado)) return true;
                // Buscamos dentro de la lista de objetos Direccion
                boolean coincidenciaDir = persona.getDirecciones().stream()
                    .anyMatch(d -> d.getDireccionCompleta().toLowerCase().contains(textoBuscado));
                if (coincidenciaDir) return true;
                
                return false;
            });
        });

        SortedList<Persona> datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaPersonas.comparatorProperty());
        tablaPersonas.setItems(datosOrdenados);

        // Escuchar clics en la tabla
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> seleccionarPersona(newValue)
        );
    }

    private void cargarTabla() {
        listaPersonas.setAll(personaDB.listar());
    }

    @FXML
    private void guardarPersona() {
        String nombre = txtNombre.getText();
        String direccionTxt = txtDireccion.getText();
        List<String> telefonos = Arrays.asList(txtTelefonos.getText().split(","))
                                       .stream()
                                       .map(String::trim)
                                       .filter(t -> !t.isEmpty()) // Evitar teléfonos vacíos
                                       .collect(Collectors.toList());

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "El nombre no puede estar vacío.");
            return;
        }

        Persona nueva = new Persona(0, nombre);
        nueva.setTelefonos(telefonos);
        // --- SRP: El controlador transforma el texto escrito en objetos ---
        List<Direccion> listaDirs = Arrays.stream(direccionTxt.split(";"))
                                          .map(String::trim)
                                          .filter(d -> !d.isEmpty())
                                          .map(d -> {
                                              Direccion dir = new Direccion();
                                              dir.setDireccionCompleta(d);
                                              return dir;
                                          })
                                          .collect(Collectors.toList());
        nueva.setDirecciones(listaDirs);
        
        if (personaDB.insertar(nueva)) {
            mostrarAlerta("Éxito", "Persona guardada correctamente.");
            limpiarFormulario();
            cargarTabla();
        }
    }

    @FXML
    private void modificarPersona() {
        if (personaSeleccionada == null) {
            mostrarAlerta("Atención", "Primero selecciona una persona de la tabla para editar.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Persona");
        dialog.setHeaderText("Modificando a: " + personaSeleccionada.getNombre() + "\n(Separa múltiples direcciones con punto y coma ';')");

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        TextField editNombre = new TextField(personaSeleccionada.getNombre());
        // Preparar el texto de direcciones para que se vea bien al editar
        String dirsActuales = personaSeleccionada.getDirecciones().stream()
                                .map(Direccion::getDireccionCompleta)
                                .collect(Collectors.joining("; "));
        TextField editDireccion = new TextField(dirsActuales);
        TextArea editTelefonos = new TextArea(String.join(", ", personaSeleccionada.getTelefonos()));
        editTelefonos.setPrefRowCount(3);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(editNombre, 1, 0);
        grid.add(new Label("Direcciones (;):"), 0, 1);
        grid.add(editDireccion, 1, 1);
        grid.add(new Label("Teléfonos (,):"), 0, 2);
        grid.add(editTelefonos, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialog.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == btnGuardar) {
            if (editNombre.getText().isEmpty()) return;

            personaSeleccionada.setNombre(editNombre.getText());
            
            List<String> nuevosTelefonos = Arrays.asList(editTelefonos.getText().split(","))
                                                 .stream().map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList());
            personaSeleccionada.setTelefonos(nuevosTelefonos);
            
            List<Direccion> nuevasDirs = Arrays.stream(editDireccion.getText().split(";"))
                                               .map(String::trim).filter(d -> !d.isEmpty())
                                               .map(d -> {
                                                   Direccion dir = new Direccion();
                                                   dir.setDireccionCompleta(d);
                                                   return dir;
                                               })
                                               .collect(Collectors.toList());
            personaSeleccionada.setDirecciones(nuevasDirs);

            if (personaDB.actualizar(personaSeleccionada)) {
                mostrarAlerta("Éxito", "Persona actualizada correctamente.");
                cargarTabla(); 
                tablaPersonas.getSelectionModel().clearSelection(); 
                personaSeleccionada = null;
            }
        }
    }

    @FXML
    private void verDetalles() {
        if (personaSeleccionada != null) {
            // Extraer las direcciones de la lista de objetos para mostrarlas bonito
            List<String> listaDirecciones = personaSeleccionada.getDirecciones().stream()
                                            .map(Direccion::getDireccionCompleta)
                                            .collect(Collectors.toList());

            String strDirecciones = listaDirecciones.isEmpty() ? "Ninguna" : String.join("\n - ", listaDirecciones);
            String strTelefonos = personaSeleccionada.getTelefonos().isEmpty() ? "Ninguno" : String.join("\n - ", personaSeleccionada.getTelefonos());

            String detalles = "ID: " + personaSeleccionada.getId() + "\n"
                            + "Nombre: " + personaSeleccionada.getNombre() + "\n\n"
                            + "Direcciones:\n - " + strDirecciones + "\n\n"
                            + "Teléfonos:\n - " + strTelefonos;
            
            mostrarAlerta("Detalles de " + personaSeleccionada.getNombre(), detalles);
        } else {
            mostrarAlerta("Atención", "Selecciona una persona de la tabla para ver sus detalles.");
        }
    }

    @FXML
    private void eliminarPersona() {
        if (personaSeleccionada != null) {
            if (personaDB.eliminar(personaSeleccionada.getId())) {
                mostrarAlerta("Éxito", "Persona eliminada correctamente.");
                limpiarFormulario();
                cargarTabla();
            }
        } else {
            mostrarAlerta("Atención", "Selecciona una persona de la tabla para eliminarla.");
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtNombre.clear();
        txtDireccion.clear();
        txtTelefonos.clear();
        personaSeleccionada = null; 
        tablaPersonas.getSelectionModel().clearSelection();
    }

    private void seleccionarPersona(Persona p) {
        if (p != null) {
            personaSeleccionada = p;
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}