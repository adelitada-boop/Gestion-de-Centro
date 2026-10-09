package dao;

import Conexion.ConexionBD;
import model.Asignatura.Asignatura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AsignaturaDAOImpl {

    // 1. Listar todas las asignaturas
    public List<Asignatura> listarTodas() {
        List<Asignatura> lista = new ArrayList<>();
        String sql = "SELECT idAsignatura, nombre FROM Asignatura";

        try (Connection conn = ConexionBD.getInstancia();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                Asignatura a = new Asignatura(nombre);
                lista.add(a);
            }
        } catch (SQLException e) {
            System.err.println("[AsignaturaDAO] Error al listar asignaturas: " + e.getMessage());
        }
        return lista;
    }

    // 2. Insertar (Alta)
    public boolean insertar(Asignatura asignatura) {
        String sql = "INSERT INTO Asignatura (nombre) VALUES (?)";

        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, asignatura.getNombre());
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("[AsignaturaDAO] Error al insertar asignatura: " + e.getMessage());
            return false;
        }
    }

    // 3. Eliminar (Baja) por nombre
    public boolean eliminar(String nombre) {
        String sql = "DELETE FROM Asignatura WHERE LOWER(nombre) = LOWER(?)";

        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre.trim());
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("[AsignaturaDAO] Error al eliminar asignatura: " + e.getMessage());
            return false;
        }
    }

    // 4. Modificar
    public boolean modificar(String nombreViejo, String nombreNuevo) {
        String sql = "UPDATE Asignatura SET nombre = ? WHERE LOWER(nombre) = LOWER(?)";

        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreNuevo.trim());
            pstmt.setString(2, nombreViejo.trim());
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("[AsignaturaDAO] Error al modificar asignatura: " + e.getMessage());
            return false;
        }
    }

    // 5. Mostrar asignaturas con sus docentes asignados (usando la relación directa en BD)
    public void mostrarAsignaturasConDocentes() {
        String sql = "SELECT a.nombre as nombreAsignatura, d.cedula, d.nombre as nombreDocente, d.cargaHoraria " +
                "FROM Asignatura a " +
                "LEFT JOIN docente d ON a.idAsignatura = d.idAsignatura " +
                "ORDER BY a.nombre, d.nombre";

        try (Connection conn = ConexionBD.getInstancia();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n==========================================");
            System.out.println("      ASIGNATURAS Y SUS DOCENTES");
            System.out.println("==========================================");

            String asignaturaActual = "";
            boolean hayDatos = false;

            while (rs.next()) {
                hayDatos = true;
                String asig = rs.getString("nombreAsignatura");
                String cedula = rs.getString("cedula");
                String nombreDoc = rs.getString("nombreDocente");
                int carga = rs.getInt("cargaHoraria");

                if (!asig.equals(asignaturaActual)) {
                    asignaturaActual = asig;
                    System.out.println("\n📚 Asignatura: " + asignaturaActual);
                    System.out.println("------------------------------------------");
                }

                if (cedula != null) {
                    System.out.println("   - " + nombreDoc + " (Cédula: " + cedula + " | Carga: " + carga + " hrs)");
                } else {
                    System.out.println("   [Sin docentes asignados]");
                }
            }

            if (!hayDatos) {
                System.out.println("No hay asignaturas registradas en el sistema.");
            }
            System.out.println("\n==========================================");

        } catch (SQLException e) {
            System.err.println("[AsignaturaDAO] Error al mostrar asignaturas con docentes: " + e.getMessage());
        }
    }

    // 6. Asignar docente a una asignatura (actualizando la tabla docente)
    public boolean asignarDocenteAAsignatura(Long idAsignatura, String cedulaDocente) {
        String sql = "UPDATE docente SET idAsignatura = ? WHERE cedula = ?";
        Connection conn = ConexionBD.getInstancia();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, idAsignatura);
            pstmt.setString(2, cedulaDocente);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("[AsignaturaDAO] Error al asignar docente a la asignatura: " + e.getMessage());
            return false;
        }
    }
}