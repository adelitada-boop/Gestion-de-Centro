package Vista;

import javax.swing.*;
import java.awt.*;

public class MenuNavegacion {

    // Método tradicional para el JFrame (por si lo mantiene)
    public static JMenuBar crearMenu(JFrame ventanaActual) {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuNavegacion = new JMenu("📌 Menú de Navegación");

        JMenuItem itemAsignaturas = new JMenuItem("Gestión de Asignaturas");
        JMenuItem itemDocentes = new JMenuItem("Gestión de Docentes");
        JMenuItem itemInasistencias = new JMenuItem("Control de Inasistencias");
        JMenuItem itemMenuPrincipal = new JMenuItem("🏠 Volver al Menú Principal");

        itemAsignaturas.addActionListener(e -> abrirAsignaturas(ventanaActual));
        itemDocentes.addActionListener(e -> abrirDocentes(ventanaActual));
        itemInasistencias.addActionListener(e -> abrirInasistencias(ventanaActual));
        itemMenuPrincipal.addActionListener(e -> abrirMenuPrincipal(ventanaActual));

        menuNavegacion.add(itemAsignaturas);
        menuNavegacion.add(itemDocentes);
        menuNavegacion.add(itemInasistencias);
        menuNavegacion.addSeparator();
        menuNavegacion.add(itemMenuPrincipal);

        menuBar.add(menuNavegacion);
        return menuBar;
    }

    // NUEVO: Panel de navegación visual en la parte superior (100% visible)
    public static JPanel crearPanelNavegacion(JFrame ventanaActual) {
        JPanel panelNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelNav.setBackground(new Color(230, 235, 245));
        panelNav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton btnAsig = new JButton("📚 Asignaturas");
        JButton btnDoc = new JButton("👨‍🏫 Docentes");
        JButton btnInas = new JButton("📊 Inasistencias");
        JButton btnHome = new JButton("🏠 Menú Principal");

        btnAsig.addActionListener(e -> abrirAsignaturas(ventanaActual));
        btnDoc.addActionListener(e -> abrirDocentes(ventanaActual));
        btnInas.addActionListener(e -> abrirInasistencias(ventanaActual));
        btnHome.addActionListener(e -> abrirMenuPrincipal(ventanaActual));

        panelNav.add(btnHome);
        panelNav.add(btnAsig);
        panelNav.add(btnDoc);
        panelNav.add(btnInas);

        return panelNav;
    }

    private static void abrirAsignaturas(JFrame ventanaActual) {
        ventanaActual.dispose();
        JFrame frame = new JFrame("Gestión de Asignaturas");
        vstAsignatura vista = new vstAsignatura();
        frame.setContentPane(vista.getPanelAsignatura());
        frame.setJMenuBar(crearMenu(frame));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void abrirDocentes(JFrame ventanaActual) {
        ventanaActual.dispose();
        JFrame frame = new JFrame("Gestión de Docentes");
        vstDocente vista = new vstDocente();
        frame.setContentPane(vista.getPanelDocente());
        frame.setJMenuBar(crearMenu(frame));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void abrirInasistencias(JFrame ventanaActual) {
        ventanaActual.dispose();
        JFrame frame = new JFrame("Control de Inasistencias");
        vstInasistencias vistaIn = new vstInasistencias();
        frame.setContentPane(vistaIn.getPanelInasistencias());
        frame.setJMenuBar(crearMenu(frame));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void abrirMenuPrincipal(JFrame ventanaActual) {
        ventanaActual.dispose();
        JFrame frame = new JFrame("Sistema de Gestión - Menú Principal");
        GestionLiceo menuPrincipal = new GestionLiceo();
        frame.setContentPane(menuPrincipal.Apariencia);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}