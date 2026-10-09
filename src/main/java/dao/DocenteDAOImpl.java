package dao;

import Conexion.ConexionBD;
import model.Docente.*;
import model.Asignatura.Asignatura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DocenteDAOImpl {

    // 1. Listar todos los docentes y sus asignaturas desde PostgreSQL en un Map
    public Map<String, Docentes> listarDocentes() {
        Map<String, Docentes> mapa = new TreeMap<>();

        String sql = "SELECT d.idDocente, d.cedula, d.nombre, d.cargaHoraria, d.sueldoBase, " +
                "d.nroActa as actaDocente, d.anioIngreso, a.nombre as nombreAsignatura, " +
                "s.nroActa as actaSuplente, s.ciTitular, " +
                "CASE " +
                "  WHEN e.idDocente IS NOT NULL THEN 'EFECTIVO' " +
                "  WHEN i.idDocente IS NOT NULL THEN 'INTERINO' " +
                "  WHEN s.idDocente IS NOT NULL THEN 'SUPLENTE' " +
                "  ELSE 'INTERINO' " +
                "END as tipodocente " +
                "FROM docente d " +
                "LEFT JOIN asignatura a ON d.idAsignatura = a.idAsignatura " +
                "LEFT JOIN docente_efectivo e ON d.idDocente = e.idDocente " +
                "LEFT JOIN docente_interino i ON d.idDocente = i.idDocente " +
                "LEFT JOIN docente_suplente s ON d.idDocente = s.idDocente";
        try (Connection conn = ConexionBD.getInstancia();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String ci = rs.getString("cedula");
                String nombre = rs.getString("nombre");
                int carga = rs.getInt("cargaHoraria");
                double sueldo = rs.getDouble("sueldoBase");
                String tipo = rs.getString("tipodocente");
                String nroActa = rs.getString("actaDocente");
                int anioIngreso = rs.getInt("anioIngreso");

                Docentes doc = null;
                switch (tipo) {
                    case "EFECTIVO":
                        doc = new DocenteEfectivo(ci, nombre, carga, sueldo, anioIngreso, nroActa);
                        break;
                    case "INTERINO":
                        doc = new DocenteInterino(ci, nombre, carga, sueldo, nroActa, anioIngreso);
                        break;
                    case "SUPLENTE":
                        String actaSup = rs.getString("actaSuplente");
                        if (actaSup == null) actaSup = nroActa;
                        String ciTit = rs.getString("ciTitular");
                        doc = new DocenteSuplente(ci, nombre, carga, sueldo, actaSup, ciTit, anioIngreso);
                        break;
                }

                if (doc != null) {
                    String nombreAsig = rs.getString("nombreAsignatura");
                    if (nombreAsig != null && !nombreAsig.isEmpty()) {
                        doc.agregarAsignatura(new Asignatura(nombreAsig));
                    }
                    cargarAsignaturasDeDocente(conn, doc);
                    mapa.put(ci, doc);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DocenteDAO] Error al listar docentes: " + e.getMessage());
        }
        return mapa;
    }

    private void cargarAsignaturasDeDocente(Connection conn, Docentes doc) {
        String sql = "SELECT nombre_asignatura FROM DocenteAsignatura WHERE cedula_docente = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doc.getCedula());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String nomAsig = rs.getString("nombre_asignatura");
                    boolean yaExiste = false;
                    for (Asignatura a : doc.getAsignaturas()) {
                        if (a.getNombre().equalsIgnoreCase(nomAsig)) {
                            yaExiste = true;
                            break;
                        }
                    }
                    if (!yaExiste) {
                        doc.agregarAsignatura(new Asignatura(nomAsig));
                    }
                }
            }
        } catch (SQLException e) {
            // Ignorar si la tabla no existe
        }
    }

    // Método requerido por tu vista para retornar una List<Docentes> directamente
    public List<Docentes> listarTodos() {
        Map<String, Docentes> mapaDocentes = listarDocentes();
        return new ArrayList<>(mapaDocentes.values());
    }

    // 2. Insertar (Alta)
    public boolean insertar(Docentes doc) {
        String sqlDocente = "INSERT INTO docente (nroActa, cedula, nombre, cargaHoraria, sueldoBase, anioIngreso) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConexionBD.getInstancia();
            conn.setAutoCommit(false);

            int idDocenteGenerado = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDocente, Statement.RETURN_GENERATED_KEYS)) {
                String nroActa = "";
                if (doc instanceof DocenteEfectivo) nroActa = ((DocenteEfectivo) doc).getNroActa();
                else if (doc instanceof DocenteInterino) nroActa = ((DocenteInterino) doc).getNroActa();
                else if (doc instanceof DocenteSuplente) nroActa = ((DocenteSuplente) doc).getNroActa();

                pstmt.setString(1, nroActa);
                pstmt.setString(2, doc.getCedula());
                pstmt.setString(3, doc.getNombre());
                pstmt.setInt(4, doc.getCargaHoraria());
                pstmt.setDouble(5, doc.getSueldoBase());
                pstmt.setInt(6, doc.getAnioIngreso());

                int filasAfectadas = pstmt.executeUpdate();
                if (filasAfectadas > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            idDocenteGenerado = rs.getInt(1);
                        }
                    }
                }
            }

            if (idDocenteGenerado == -1) {
                conn.rollback();
                return false;
            }

            if (doc instanceof DocenteEfectivo) {
                String sqlEf = "INSERT INTO docente_efectivo (idDocente) VALUES (?)";
                try (PreparedStatement pstmtEf = conn.prepareStatement(sqlEf)) {
                    pstmtEf.setInt(1, idDocenteGenerado);
                    pstmtEf.executeUpdate();
                }
            } else if (doc instanceof DocenteInterino) {
                String sqlInt = "INSERT INTO docente_interino (idDocente) VALUES (?)";
                try (PreparedStatement pstmtInt = conn.prepareStatement(sqlInt)) {
                    pstmtInt.setInt(1, idDocenteGenerado);
                    pstmtInt.executeUpdate();
                }
            } else if (doc instanceof DocenteSuplente) {
                String sqlSup = "INSERT INTO docente_suplente (nroActa, ciTitular, idDocente) VALUES (?, ?, ?)";
                try (PreparedStatement pstmtSup = conn.prepareStatement(sqlSup)) {
                    DocenteSuplente sup = (DocenteSuplente) doc;
                    pstmtSup.setString(1, sup.getNroActa());
                    pstmtSup.setString(2, sup.getCedulaTitularReemplazado());
                    pstmtSup.setInt(3, idDocenteGenerado);
                    pstmtSup.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[DocenteDAO] Error al insertar docente: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // 3. Eliminar (Baja)
    public boolean eliminar(String ci) {
        String sql = "DELETE FROM docente WHERE cedula = ?";
        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ci.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DocenteDAO] Error al eliminar docente: " + e.getMessage());
            return false;
        }
    }

    // 4. Modificar nombre
    public boolean modificar(String ci, String nuevoNombre) {
        String sql = "UPDATE docente SET nombre = ? WHERE cedula = ?";
        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoNombre.trim());
            pstmt.setString(2, ci.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DocenteDAO] Error al modificar docente: " + e.getMessage());
            return false;
        }
    }

    // 5. Asociar asignatura a docente
    public boolean asignarAsignatura(String ci, String nombreAsignatura) {
        String sql = "INSERT INTO DocenteAsignatura (cedula_docente, nombre_asignatura) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ci);
            pstmt.setString(2, nombreAsignatura);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DocenteDAO] Error al asociar asignatura: " + e.getMessage());
            return false;
        }
    }

    // 6. Listar docentes filtrados por asignatura
    public Map<String, Docentes> listarPorAsignatura(String nombreAsignaturaBuscada) {
        Map<String, Docentes> mapa = new TreeMap<>();

        String sql = "SELECT d.idDocente, d.cedula, d.nombre, d.cargaHoraria, d.sueldoBase, " +
                "d.nroActa as actaDocente, d.anioIngreso, a.nombre as nombreAsignatura, " +
                "s.nroActa as actaSuplente, s.ciTitular, " +
                "CASE " +
                "  WHEN e.idDocente IS NOT NULL THEN 'EFECTIVO' " +
                "  WHEN i.idDocente IS NOT NULL THEN 'INTERINO' " +
                "  WHEN s.idDocente IS NOT NULL THEN 'SUPLENTE' " +
                "  ELSE 'INTERINO' " +
                "END as tipodocente " +
                "FROM docente d " +
                "JOIN asignatura a ON d.idAsignatura = a.idAsignatura " +
                "LEFT JOIN docente_efectivo e ON d.idDocente = e.idDocente " +
                "LEFT JOIN docente_interino i ON d.idDocente = i.idDocente " +
                "LEFT JOIN docente_suplente s ON d.idDocente = s.idDocente " +
                "WHERE LOWER(a.nombre) LIKE LOWER(?)";

        try (Connection conn = ConexionBD.getInstancia();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String parametroBusqueda = "%" + nombreAsignaturaBuscada.trim() + "%";
            pstmt.setString(1, parametroBusqueda);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String ci = rs.getString("cedula");
                    String nombre = rs.getString("nombre");
                    int carga = rs.getInt("cargaHoraria");
                    double sueldo = rs.getDouble("sueldoBase");
                    String tipo = rs.getString("tipodocente");
                    String nroActa = rs.getString("actaDocente");
                    int anioIngreso = rs.getInt("anioIngreso");

                    Docentes doc = null;
                    switch (tipo) {
                        case "EFECTIVO":
                            doc = new DocenteEfectivo(ci, nombre, carga, sueldo, anioIngreso, nroActa);
                            break;
                        case "INTERINO":
                            doc = new DocenteInterino(ci, nombre, carga, sueldo, nroActa, anioIngreso);
                            break;
                        case "SUPLENTE":
                            String actaSup = rs.getString("actaSuplente");
                            if (actaSup == null) actaSup = nroActa;
                            String ciTit = rs.getString("ciTitular");
                            doc = new DocenteSuplente(ci, nombre, carga, sueldo, actaSup, ciTit, anioIngreso);
                            break;
                    }

                    if (doc != null) {
                        String nombreAsig = rs.getString("nombreAsignatura");
                        if (nombreAsig != null && !nombreAsig.isEmpty()) {
                            doc.agregarAsignatura(new Asignatura(nombreAsig));
                        }
                        mapa.put(ci, doc);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DocenteDAO] Error al listar docentes por asignatura: " + e.getMessage());
        }
        return mapa;
    }
}