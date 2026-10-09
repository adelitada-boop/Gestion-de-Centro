package Vista;

import dao.DocenteDAOImpl;
import model.Docente.DocenteEfectivo;
import model.Docente.DocenteInterino;
import model.Docente.DocenteSuplente;
import model.Docente.Docentes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class vstDocente {
    // Componentes principales y visuales del .form
    public JPanel panelDocente;
    private JPanel ventana;
    private JLabel lblCedula;
    private JLabel lblCargaHoraria;
    private JLabel lblTipoDocente;
    private JLabel lblNombre;
    private JLabel lblAnioIngreso;

    // Campos de texto vinculados al archivo .form
    private JTextField cédulaDocenteTextField;
    private JTextField nombreDocenteTextField;
    private JTextField cargaHorariaTextField;
    private JTextField añoDeIngresoTextField;

    // Botones y controles
    private JButton btnAgregar;
    private JButton btnModificar;
    private JButton btnEliminar;
    private JComboBox<String> cboTipoDocente;
    private JTable tblListaDocente;
    private JScrollPane ScrPnlListado;

    private DocenteDAOImpl docenteDAO;

    public vstDocente() {
        docenteDAO = new DocenteDAOImpl();
        cargarTiposDocente();

        // Cargar la tabla al abrir la ventana por primera vez
        cargarTablaDocentes();

        // Acción para el botón Agregar
        if (btnAgregar != null) {
            btnAgregar.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    agregarDocente();
                }
            });
        }
    }

    private void cargarTiposDocente() {
        if (cboTipoDocente != null) {
            cboTipoDocente.removeAllItems();
            cboTipoDocente.addItem("Efectivo");
            cboTipoDocente.addItem("Interino");
            cboTipoDocente.addItem("Suplente");
        }
    }

    private void agregarDocente() {
        try {
            // 1. Capturar y validar los campos desde el formulario
            String cedula = cédulaDocenteTextField.getText().trim();
            String nombre = nombreDocenteTextField.getText().trim();

            if (cedula.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(null, "La cédula y el nombre son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cargaHoraria = Integer.parseInt(cargaHorariaTextField.getText().trim());
            int anioIngreso = Integer.parseInt(añoDeIngresoTextField.getText().trim());

            double sueldoBase = 25000.0;

            // 2. Determinar el tipo de docente seleccionado en el ComboBox
            String tipoDocente = cboTipoDocente.getSelectedItem().toString();
            Docentes nuevoDocente;

            switch (tipoDocente.toUpperCase()) {
                case "EFECTIVO":
                    nuevoDocente = new DocenteEfectivo(cedula, nombre, cargaHoraria, sueldoBase, anioIngreso, "ACTA-EF-001");
                    break;
                case "INTERINO":
                    nuevoDocente = new DocenteInterino(cedula, nombre, cargaHoraria, sueldoBase, "ACTA-INT-001", anioIngreso);
                    break;
                case "SUPLENTE":
                    nuevoDocente = new DocenteSuplente(cedula, nombre, cargaHoraria, sueldoBase, "ACTA-SUP-001", "0000000-0", anioIngreso);
                    break;
                default:
                    nuevoDocente = new DocenteInterino(cedula, nombre, cargaHoraria, sueldoBase, "ACTA-DEF-001", anioIngreso);
                    break;
            }

            // 3. Invocar al DAO para guardar en PostgreSQL
            boolean exito = docenteDAO.insertar(nuevoDocente);

            if (exito) {
                JOptionPane.showMessageDialog(null, "¡Docente guardado exitosamente en la base de datos!", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                // Limpiar campos tras el éxito
                cédulaDocenteTextField.setText("");
                nombreDocenteTextField.setText("");
                cargaHorariaTextField.setText("");
                añoDeIngresoTextField.setText("");

                // Refrescar la tabla automáticamente
                cargarTablaDocentes();
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo registrar el docente. Revisa la consola para más detalles.", "Error de BD", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Por favor, verifica que Carga Horaria y Año de Ingreso sean números válidos.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para poblar la JTable consultando al DAO (Limpio y sin bucles)
    public void cargarTablaDocentes() {
        String[] columnas = {"Cédula", "Nombre", "Carga Horaria", "Año Ingreso"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        List<Docentes> lista = docenteDAO.listarTodos();

        if (lista != null) {
            for (Docentes d : lista) {
                modelo.addRow(new Object[]{
                        d.getCedula(),
                        d.getNombre(),
                        d.getCargaHoraria() + " hrs",
                        d.getAnioIngreso()
                });
            }
        }

        if (tblListaDocente != null) {
            tblListaDocente.setModel(modelo);
        }
    }

    public JPanel getPanelDocente() {
        return panelDocente != null ? panelDocente : ventana;
    }
}