package dao;

import model.Usuario;

import java.util.List;

public interface UsuarioDAO {
    void insertar(Usuario usuario);
    Usuario buscarPorId(Long id);
    List<Usuario> listarTodos();
    boolean eliminar(long idUsuario);
}

