package dao;
import Conexion.ConexionBD;
import model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {
    @Override
    public void insertar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, email) VALUES (?, ?)";
        Connection conn = ConexionBD.getInstancia();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.executeUpdate();
            System.out.println("[DAO] Usuario insertado correctamente.");

        } catch (SQLException e) {
            System.err.println("[DAO] Error al insertar usuario: " + e.getMessage());
        }
    }

    @Override
    public Usuario buscarPorId(Long idUsuario) {
        String sql = "SELECT * FROM usuario WHERE idUsuario = ?";
        Connection conn = ConexionBD.getInstancia();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getLong("idUsuario"),
                            rs.getString("nombre"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        Connection conn = ConexionBD.getInstancia();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getLong("idUsuario"),
                        rs.getString("nombre"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error al listar usuarios: " + e.getMessage());
        }
        return usuarios;
    }


    @Override
    public boolean eliminar(long idUsuario) {
        String sql = "DELETE FROM usuario WHERE idUsuario = ?";
        Connection conn = ConexionBD.getInstancia();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}