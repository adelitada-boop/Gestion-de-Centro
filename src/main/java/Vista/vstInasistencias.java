package Vista;

import dao.InasistenciaDAOImpl;
import model.Inasistencias.Inasistencias;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

public class vstInasistencias {
    private JTextField txtCedulaFaltas;
    private JButton btnBuscar;
    private JTextField txtFechas;
    private JComboBox<String> cboMotivo;
    private JTable tblInasistencias;
    private JButton btnModificarHrs;
    private JButton btnEliminar;
    private JButton btnAgregar;
    private JButton btnModificarCausal;

    private InasistenciaDAOImpl inasistenciaDAO;
    private DefaultTableModel modeloTabla;
    public JPanel panelInasistencias;

    public vstInasistencias() {
        if (panelInasistencias == null) {
            panelInasistencias = new JPanel(new BorderLayout());
        }

        if (!(panelInasistencias.getLayout() instanceof BorderLayout)) {
            panelInasistencias.setLayout(new BorderLayout());
        }

        JPanel panelNorte = new JPanel(new FlowLayout());
        if (txtCedulaFaltas == null) txtCedulaFaltas = new JTextField(10);
        if (btnBuscar == null) btnBuscar = new JButton("Buscar");
        if (txtFechas == null) txtFechas = new JTextField(10);
        if (cboMotivo == null) cboMotivo = new JComboBox<>();

        panelNorte.add(new JLabel("Cédula:"));
        panelNorte.add(txtCedulaFaltas);
        panelNorte.add(btnBuscar);
        panelNorte.add(new JLabel("Fecha (YYYY-MM-DD):"));
        panelNorte.add(txtFechas);
        panelNorte.add(new JLabel("Motivo:"));
        panelNorte.add(cboMotivo);

        if (((BorderLayout) panelInasistencias.getLayout()).getLayoutComponent(BorderLayout.NORTH) == null) {
            panelInasistencias.add(panelNorte, BorderLayout.NORTH);
        }

        if (tblInasistencias == null) {
            tblInasistencias = new JTable();
        }
        if (((BorderLayout) panelInasistencias.getLayout()).getLayoutComponent(BorderLayout.CENTER) == null) {
            panelInasistencias.add(new JScrollPane(tblInasistencias), BorderLayout.CENTER);
        }

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        if (btnAgregar == null) btnAgregar = new JButton("Agregar");
        if (btnModificarHrs == null) btnModificarHrs = new JButton("Modificar Horas");
        if (btnModificarCausal == null) btnModificarCausal = new JButton("Modificar Causal");
        if (btnEliminar == null) btnEliminar = new JButton("Eliminar");

        panelSur.add(btnAgregar);
        panelSur.add(btnModificarHrs);
        panelSur.add(btnModificarCausal);
        panelSur.add(btnEliminar);

        if (((BorderLayout) panelInasistencias.getLayout()).getLayoutComponent(BorderLayout.SOUTH) == null) {
            panelInasistencias.add(panelSur, BorderLayout.SOUTH);
        }

        inasistenciaDAO = new InasistenciaDAOImpl();
        inicializarTabla();
        cargarMotivosCombo();

        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarInasistenciasDocente();
            }
        });

        btnAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarInasistencia();
            }
        });

        btnModificarHrs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarHorasInasistencia();
            }
        });

        btnModificarCausal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarCausalInasistencia();
            }
        });

        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarInasistencia();
            }
        });
    }

    private void inicializarTabla() {
        if (tblInasistencias == null) return;
        modeloTabla = new DefaultTableModel(new String[]{"Fecha", "Horas", "Motivo"}, 0);
        tblInasistencias.setModel(modeloTabla);
    }

    private void cargarMotivosCombo() {
        if (cboMotivo == null) return;
        cboMotivo.removeAllItems();
        for (Inasistencias.ArticuloInasistencia articulo : Inasistencias.ArticuloInasistencia.values()) {
            cboMotivo.addItem(articulo.name());
        }
    }

    private void buscarInasistenciasDocente() {
        String cedulaBuscada = txtCedulaFaltas.getText().trim();
        if (cedulaBuscada.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, ingresa la cédula del docente para buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);
        List<Inasistencias> lista = inasistenciaDAO.listarTodas();

        boolean encontrado = false;
        for (Inasistencias i : lista) {
            if (i.getCedulaDocente().equals(cedulaBuscada)) {
                modeloTabla.addRow(new Object[]{
                        i.getFecha(),
                        i.getHoras(),
                        i.getMotivo()
                });
                encontrado = true;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "No se encontraron inasistencias registradas para esta cédula.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void registrarInasistencia() {
        try {
            String cedula = txtCedulaFaltas.getText().trim();
            if (cedula.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Ingresa primero la cédula del docente.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fecha = LocalDate.parse(txtFechas.getText().trim());
            int horas = Integer.parseInt(JOptionPane.showInputDialog(null, "Ingrese la cantidad de horas de inasistencia:"));

            Inasistencias.ArticuloInasistencia motivo = Inasistencias.ArticuloInasistencia.valueOf(cboMotivo.getSelectedItem().toString());

            Inasistencias nuevaInasistencia = new Inasistencias(cedula, fecha, horas, motivo);
            boolean exito = inasistenciaDAO.insertar(nuevaInasistencia);

            if (exito) {
                JOptionPane.showMessageDialog(null, "Inasistencia registrada correctamente.");
                buscarInasistenciasDocente();
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo registrar. Verifica que la cédula exista en el sistema.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error en los datos (Verifica que la fecha tenga formato YYYY-MM-DD): " + ex.getMessage(), "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarHorasInasistencia() {
        int filaSeleccionada = tblInasistencias.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una inasistencia de la tabla para modificar las horas.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cedula = txtCedulaFaltas.getText().trim();
        String fecha = modeloTabla.getValueAt(filaSeleccionada, 0).toString();
        String motivoActual = modeloTabla.getValueAt(filaSeleccionada, 2).toString();

        try {
            String inputHoras = JOptionPane.showInputDialog(null, "Ingrese las nuevas horas de inasistencia:");
            if (inputHoras == null) return;
            int nuevasHoras = Integer.parseInt(inputHoras.trim());

            boolean exito = inasistenciaDAO.modificar(cedula, fecha, nuevasHoras, motivoActual);

            if (exito) {
                JOptionPane.showMessageDialog(null, "Horas modificadas con éxito.");
                buscarInasistenciasDocente();
            } else {
                JOptionPane.showMessageDialog(null, "Error al modificar en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Por favor ingrese un valor numérico válido para las horas.", "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarCausalInasistencia() {
        int filaSeleccionada = tblInasistencias.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una inasistencia de la tabla para modificar la causal.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cedula = txtCedulaFaltas.getText().trim();
        String fecha = modeloTabla.getValueAt(filaSeleccionada, 0).toString();
        int horasActuales = Integer.parseInt(modeloTabla.getValueAt(filaSeleccionada, 1).toString());

        Inasistencias.ArticuloInasistencia[] motivos = Inasistencias.ArticuloInasistencia.values();
        Inasistencias.ArticuloInasistencia motivoSeleccionado = (Inasistencias.ArticuloInasistencia) JOptionPane.showInputDialog(
                null,
                "Seleccione la nueva causal/artículo:",
                "Modificar Causal",
                JOptionPane.QUESTION_MESSAGE,
                null,
                motivos,
                motivos[0]
        );

        if (motivoSeleccionado == null) return;

        boolean exito = inasistenciaDAO.modificar(cedula, fecha, horasActuales, motivoSeleccionado.name());

        if (exito) {
            JOptionPane.showMessageDialog(null, "Causal modificada con éxito.");
            buscarInasistenciasDocente();
        } else {
            JOptionPane.showMessageDialog(null, "Error al modificar en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarInasistencia() {
        int filaSeleccionada = tblInasistencias.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una inasistencia de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cedula = txtCedulaFaltas.getText().trim();
        if (cedula.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, ingresa o mantén la cédula del docente en el campo superior.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fecha = modeloTabla.getValueAt(filaSeleccionada, 0).toString();

        int confirmacion = JOptionPane.showConfirmDialog(null, "¿Estás segura de eliminar esta inasistencia?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean exito = inasistenciaDAO.eliminar(cedula, fecha);
            if (exito) {
                JOptionPane.showMessageDialog(null, "Inasistencia eliminada correctamente.");
                buscarInasistenciasDocente();
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar el registro. Verifica los datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public JPanel getPanelInasistencias() {
        return panelInasistencias;
    }
}