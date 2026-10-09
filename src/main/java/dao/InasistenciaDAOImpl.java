package dao;

import Conexion.ConexionBD;
import model.Inasistencias.Inasistencias;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InasistenciaDAOImpl {

    // 1. Listar todas las inasistencias desde la base de datos
    public List<Inasistencias> listarTodas() {
        List<Inasistencias> lista = new ArrayList<>();
        String sql = "SELECT f.idFaltas, d.cedula, f.fecha, f.horas, f.motivo " +
                "FROM inasistencias f " +
                "JOIN docente d ON f.idDocente = d.idDocente";

        try (Connection conn = ConexionBD.getInstancia();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String ci = rs.getString("cedula");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                int horas = rs.getInt("horas");
                String strMotivo = rs.getString("motivo");

                Inasistencias.ArticuloInasistencia motivo = Inasistencias.ArticuloInasistencia.valueOf(strMotivo);

                Inasistencias inasistencia = new Inasistencias(ci, fecha, horas, motivo);
                lista.add(inasistencia);
            }
        } catch (SQLException e) {
            System.err.println("[InasistenciaDAO] Error al listar inasistencias: " + e.getMessage());
        }
        return lista;
    }

    // 2. Insertar (Alta) buscando primero el idDocente por su cédula
    public boolean insertar(Inasistencias inasistencia) {
        String sqlBuscarId = "SELECT idDocente FROM docente WHERE cedula = ?";
        String sqlInsert = "INSERT INTO inasistencias (idDocente, fecha, horas, motivo) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getInstancia()) {
            int idDocente = -1;

            try (PreparedStatement pstmtId = conn.prepareStatement(sqlBuscarId)) {
                pstmtId.setString(1, inasistencia.getCedulaDocente().trim());
                try (ResultSet rs = pstmtId.executeQuery()) {
                    if (rs.next()) {
                        idDocente = rs.getInt("idDocente");
                    }
                }
            }

            if (idDocente == -1) {
                System.err.println("[InasistenciaDAO] No se encontró un docente con la cédula: " + inasistencia.getCedulaDocente());
                return false;
            }

            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setInt(1, idDocente);
                pstmtInsert.setDate(2, Date.valueOf(inasistencia.getFecha()));
                pstmtInsert.setInt(3, inasistencia.getHoras());
                pstmtInsert.setString(4, inasistencia.getMotivo().name());

                return pstmtInsert.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("[InasistenciaDAO] Error al insertar inasistencia: " + e.getMessage());
            return false;
        }
    }

    // 3. Modificar solo las horas (sin asignatura)
    public boolean modificarHoras(String ci, String fecha, int nuevasHoras) {
        String sql = "UPDATE inasistencias SET horas = ? " +
                "FROM docente d " +
                "WHERE inasistencias.idDocente = d.idDocente " +
                "AND d.cedula = ? AND inasistencias.fecha = ?";

        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevasHoras);
            pstmt.setString(2, ci.trim());
            pstmt.setDate(3, Date.valueOf(fecha));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InasistenciaDAO] Error al modificar inasistencia: " + e.getMessage());
            return false;
        }
    }

    // 4. Eliminar (Baja) (sin asignatura)
    public boolean eliminar(String ci, String fecha) {
        String sql = "DELETE FROM inasistencias " +
                "USING docente d " +
                "WHERE inasistencias.idDocente = d.idDocente " +
                "AND d.cedula = ? AND inasistencias.fecha = ?";

        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ci.trim());
            pstmt.setDate(2, Date.valueOf(fecha));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InasistenciaDAO] Error al eliminar inasistencia: " + e.getMessage());
            return false;
        }
    }

    // 5. Modificar tanto las horas como el motivo (sin asignatura)
    public boolean modificar(String cedulaDocente, String fecha, int nuevasHoras, String nuevoMotivo) {
        String sql = "UPDATE inasistencias SET horas = ?, motivo = ? " +
                "FROM docente d WHERE inasistencias.idDocente = d.idDocente " +
                "AND d.cedula = ? AND inasistencias.fecha = ?::date";

        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevasHoras);
            pstmt.setString(2, nuevoMotivo);
            pstmt.setString(3, cedulaDocente.trim());
            pstmt.setDate(4, Date.valueOf(fecha.trim()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InasistenciaDAO] Error al modificar inasistencia (horas y motivo): " + e.getMessage());
            return false;
        }
    }
}