package Vista;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionLiceo {
    public JPanel Apariencia;
    private JButton btnDocente;
    private JButton btnAsignatura;
    private JButton btnInasistencia;
    private JButton btnUsuario;
    private JButton btnSalir;
    private JButton btnEliminar;

    public GestionLiceo() {
        // Módulo de Docentes
        btnDocente.addActionListener(e -> {
            JFrame frameDocente = new JFrame("Gestión de Docentes");
            vstDocente vistaDocente = new vstDocente();
            frameDocente.setContentPane(vistaDocente.panelDocente);

            // Inyectar el menú de navegación y refrescar
            frameDocente.setJMenuBar(MenuNavegacion.crearMenu(frameDocente));
            frameDocente.revalidate();
            frameDocente.repaint();

            frameDocente.setSize(700, 500);
            frameDocente.setLocationRelativeTo(Apariencia);
            frameDocente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frameDocente.setVisible(true);
        });

        // Módulo de Asignaturas
        btnAsignatura.addActionListener(e -> {
            JFrame frameAsignatura = new JFrame("Gestión de Asignaturas");
            vstAsignatura vista = new vstAsignatura();
            if (vista.panelAsignatura != null) {
                frameAsignatura.setContentPane(vista.panelAsignatura);

                // Inyectar el menú de navegación y refrescar
                frameAsignatura.setJMenuBar(MenuNavegacion.crearMenu(frameAsignatura));
                frameAsignatura.revalidate();
                frameAsignatura.repaint();

                frameAsignatura.setSize(700, 500);
                frameAsignatura.setLocationRelativeTo(null);
                frameAsignatura.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frameAsignatura.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Error: El panel de asignaturas es nulo.");
            }
        });

        // Módulo de Inasistencias
        btnInasistencia.addActionListener(e -> {
            JFrame frameInasistencia = new JFrame("Control de Inasistencias");
            vstInasistencias vistaIn = new vstInasistencias();

            if (vistaIn.panelInasistencias != null) {
                frameInasistencia.setContentPane(vistaIn.panelInasistencias);

                // Inyectar el menú de navegación y refrescar
                frameInasistencia.setJMenuBar(MenuNavegacion.crearMenu(frameInasistencia));
                frameInasistencia.revalidate();
                frameInasistencia.repaint();

                frameInasistencia.setSize(750, 550);
                frameInasistencia.setLocationRelativeTo(Apariencia);
                frameInasistencia.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frameInasistencia.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(Apariencia, "Error: El panel de inasistencias es nulo.");
            }
        });

        // Módulo de Usuarios (Ver y Eliminar)
        btnUsuario.addActionListener(e -> {
            JFrame frameUsuario = new JFrame("Gestión de Usuarios y Operadores");
            JPanel panelDinamico = new JPanel(new BorderLayout());

            DefaultTableModel modeloTablaUsuarios = new DefaultTableModel(new String[]{"ID", "Nombre", "Email"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable tblUsuarios = new JTable(modeloTablaUsuarios);
            JScrollPane scrollPane = new JScrollPane(tblUsuarios);
            panelDinamico.add(scrollPane, BorderLayout.CENTER);

            // Cargar usuarios de forma segura
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            for (Usuario u : usuarios) {
                modeloTablaUsuarios.addRow(new Object[]{u.getId(), u.getNombre(), u.getEmail()});
            }

            JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            JButton btnEliminarUsuario = new JButton("Eliminar Seleccionado");
            panelSur.add(btnEliminarUsuario);
            panelDinamico.add(panelSur, BorderLayout.SOUTH);

            btnEliminarUsuario.addActionListener(ev -> {
                int filaSeleccionada = tblUsuarios.getSelectedRow();
                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(frameUsuario, "Por favor, selecciona un usuario de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Long idUsuario = (Long) modeloTablaUsuarios.getValueAt(filaSeleccionada, 0);
                String nombreUsuario = modeloTablaUsuarios.getValueAt(filaSeleccionada, 1).toString();

                int confirm = JOptionPane.showConfirmDialog(
                        frameUsuario,
                        "¿Estás seguro de eliminar al operador: " + nombreUsuario + "?",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean exito = usuarioDAO.eliminar(idUsuario);
                    if (exito) {
                        JOptionPane.showMessageDialog(frameUsuario, "Usuario eliminado con éxito.");
                        modeloTablaUsuarios.setRowCount(0);
                        List<Usuario> listaActualizada = usuarioDAO.listarTodos();
                        for (Usuario u : listaActualizada) {
                            modeloTablaUsuarios.addRow(new Object[]{u.getId(), u.getNombre(), u.getEmail()});
                        }
                    } else {
                        JOptionPane.showMessageDialog(frameUsuario, "Error al intentar eliminar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            frameUsuario.setContentPane(panelDinamico);

            // Inyectar el menú de navegación y refrescar
            frameUsuario.setJMenuBar(MenuNavegacion.crearMenu(frameUsuario));
            frameUsuario.revalidate();
            frameUsuario.repaint();

            frameUsuario.setSize(600, 400);
            frameUsuario.setLocationRelativeTo(Apariencia);
            frameUsuario.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frameUsuario.setVisible(true);
        });

        // Botón Salir / Cerrar sistema
        if (btnSalir != null) {
            btnSalir.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        Apariencia,
                        "¿Desea salir del sistema?",
                        "Confirmar Salida",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            });
        }
    }
}