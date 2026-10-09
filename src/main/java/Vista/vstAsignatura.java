package Vista;

import Conexion.ConexionBD;
import dao.AsignaturaDAOImpl;
import model.Asignatura.Asignatura;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class vstAsignatura {
    public JPanel pnlAsignatura;
    public JPanel panelAsignatura;
    private JScrollPane sclpnalAsignatura;
    private JScrollPane scrPnlAsignatura;
    private JTable tblAsignatura;
    private JScrollPane srllpnlAsignaturaporDocente;
    private JTable tblDocenteporAsigantura;
    public JButton btnAgregarDocAsignatura;

    // Campo de texto vinculado exactamente al .form
    private JTextField textCedulaDocente;

    private AsignaturaDAOImpl asignaturaDAO;

    public vstAsignatura() {
        this.asignaturaDAO = new AsignaturaDAOImpl();

        // 1. Crear un panel totalmente limpio con BorderLayout estándar de Java
        pnlAsignatura = new JPanel();
        pnlAsignatura.setLayout(new BorderLayout(10, 10));
        panelAsignatura = pnlAsignatura;

        // 2. Inicializar componentes si son nulos
        if (tblAsignatura == null) tblAsignatura = new JTable();
        if (tblDocenteporAsigantura == null) tblDocenteporAsigantura = new JTable();
        if (textCedulaDocente == null) textCedulaDocente = new JTextField(10);
        if (btnAgregarDocAsignatura == null) btnAgregarDocAsignatura = new JButton("Asignar Docente a Asignatura");

        // 3. Crear paneles organizados
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.add(new JLabel("Cédula Docente:"));
        panelSuperior.add(textCedulaDocente);
        panelSuperior.add(btnAgregarDocAsignatura);

        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 10, 10));
        panelTablas.add(new JScrollPane(tblAsignatura));
        panelTablas.add(new JScrollPane(tblDocenteporAsigantura));

        // 4. Agregar al panel principal sin conflictos de GridConstraints
        pnlAsignatura.add(panelSuperior, BorderLayout.NORTH);
        pnlAsignatura.add(panelTablas, BorderLayout.CENTER);

        // 5. Cargar datos iniciales
        cargarTablaAsignaturas();
        cargarTablaDocentesPorAsignatura();

        // 6. Lógica del botón de asignación
        if (btnAgregarDocAsignatura != null) {
            btnAgregarDocAsignatura.addActionListener(e -> {
                int filaSeleccionada = tblAsignatura.getSelectedRow();
                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(pnlAsignatura, "Por favor, seleccione una asignatura de la tabla superior.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String cedulaDocente = "";
                if (textCedulaDocente != null && !textCedulaDocente.getText().trim().isEmpty()) {
                    cedulaDocente = textCedulaDocente.getText().trim();
                } else {
                    cedulaDocente = JOptionPane.showInputDialog(
                            pnlAsignatura,
                            "Ingrese la cédula del docente a asignar:",
                            "Asignar Docente",
                            JOptionPane.QUESTION_MESSAGE
                    );
                }

                if (cedulaDocente == null || cedulaDocente.trim().isEmpty()) {
                    return;
                }

                cedulaDocente = cedulaDocente.trim();
                String nombreAsignaturaSeleccionada = tblAsignatura.getValueAt(filaSeleccionada, 1).toString();
                Long idAsignatura = obtenerIdAsignaturaPorNombre(nombreAsignaturaSeleccionada);

                if (idAsignatura != null && idAsignatura > 0) {
                    boolean exito = asignaturaDAO.asignarDocenteAAsignatura(idAsignatura, cedulaDocente);

                    if (exito) {
                        JOptionPane.showMessageDialog(pnlAsignatura, "¡Asignatura asignada correctamente al docente!");
                        if (textCedulaDocente != null) textCedulaDocente.setText("");
                        cargarTablaDocentesPorAsignatura();
                    } else {
                        JOptionPane.showMessageDialog(pnlAsignatura, "Error: Verifique que la cédula del docente exista en el sistema.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(pnlAsignatura, "No se pudo identificar el ID de la asignatura.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    } // <--- Aquí cerramos correctamente el constructor

    private Long obtenerIdAsignaturaPorNombre(String nombreAsignatura) {
        String sql = "SELECT idAsignatura FROM Asignatura WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreAsignatura.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("idAsignatura");
                }
            }
        } catch (SQLException e) {
            System.err.println("[vstAsignatura] Error al buscar ID de asignatura: " + e.getMessage());
        }
        return null;
    }

    public void cargarTablaAsignaturas() {
        if (tblAsignatura == null) return;
        String[] columnas = {"ID", "Nombre"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Asignatura> lista = asignaturaDAO.listarTodas();
        int idSecuencial = 1;
        for (Asignatura a : lista) {
            modelo.addRow(new Object[]{idSecuencial++, a.getNombre()});
        }
        tblAsignatura.setModel(modelo);
    }

    public void cargarTablaDocentesPorAsignatura() {
        if (tblDocenteporAsigantura == null) return;
        String[] columnas = {"Asignatura", "Cédula Docente", "Nombre Docente", "Carga Horaria"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        String sql = "SELECT a.nombre as nombreAsignatura, d.cedula, d.nombre as nombreDocente, d.cargaHoraria " +
                "FROM Asignatura a " +
                "LEFT JOIN docente d ON a.idAsignatura = d.idAsignatura " +
                "ORDER BY a.nombre, d.nombre";

        try (Connection conn = ConexionBD.getInstancia();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String asig = rs.getString("nombreAsignatura");
                String cedula = rs.getString("cedula");
                String nombreDoc = rs.getString("nombreDocente");
                int carga = rs.getInt("cargaHoraria");

                if (cedula == null) {
                    cedula = "-";
                    nombreDoc = "[Sin docentes asignados]";
                    carga = 0;
                }
                modelo.addRow(new Object[]{asig, cedula, nombreDoc, carga + " hrs"});
            }
        } catch (SQLException ex) {
            System.err.println("[Reporte] Error al cargar docentes por asignatura: " + ex.getMessage());
        }

        tblDocenteporAsigantura.setModel(modelo);
    }

    public JPanel getPanelAsignatura() {
        return pnlAsignatura != null ? pnlAsignatura : panelAsignatura;
    }
}