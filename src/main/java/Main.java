
import Vista.GestionLiceo;
import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import model.Usuario;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField campoNombre = new JTextField();
            JTextField campoEmail = new JTextField();

            Object[] mensaje = {
                    "Nombre del operador:", campoNombre,
                    "Email del operador:", campoEmail
            };

            int opcion = JOptionPane.showConfirmDialog(null, mensaje, "Identificación de Operador", JOptionPane.OK_CANCEL_OPTION);

            if (opcion == JOptionPane.OK_OPTION) {
                String nombre = campoNombre.getText().trim();
                String email = campoEmail.getText().trim();

                if (!nombre.isEmpty() && !email.isEmpty()) {
                    // Guardamos el operador en PostgreSQL
                    Usuario nuevoUsuario = new Usuario(null, nombre, email);
                    UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
                    usuarioDAO.insertar(nuevoUsuario);

                    JOptionPane.showMessageDialog(null, "¡Bienvenido, " + nombre + "! Sesión registrada correctamente.");
                } else {
                    JOptionPane.showMessageDialog(null, "Los campos no pueden estar vacíos. Se continuará sin registrar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }

                // Abrir el Menú Principal (GestionLiceo)
                JFrame frame = new JFrame("Sistema de Gestión de Inasistencias - Menú Principal");
                GestionLiceo menuPrincipal = new GestionLiceo();
                frame.setContentPane(menuPrincipal.Apariencia); // Ajusta si el panel principal tiene otro nombre
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } else {
                System.exit(0); // Si cancela, se cierra la app
            }
        });
    }
}