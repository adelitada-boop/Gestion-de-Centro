package Conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static Connection conexion = null;

    private ConexionBD() {}

    public static Connection getInstancia() {
        try {
            if (conexion == null || conexion.isClosed()) {
                synchronized (ConexionBD.class) {
                    if (conexion == null || conexion.isClosed()) {
                        Properties propiedades = new Properties();
                        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
                            if (input == null) {
                                throw new RuntimeException("No se encontró el archivo db.properties en resources.");
                            }
                            propiedades.load(input);

                            String url = propiedades.getProperty("db.url");
                            String usuario = propiedades.getProperty("db.user");
                            String contrasena = propiedades.getProperty("db.password");
                            String driver = propiedades.getProperty("db.driver");

                            Class.forName(driver);
                            conexion = DriverManager.getConnection(url, usuario, contrasena);
                            System.out.println("[Singleton] Nueva conexión establecida con PostgreSQL.");
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            System.err.println("[Singleton] Error al inicializar la base de datos: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return conexion;
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("[Singleton] Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("[Singleton] Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
