package model;

public class Usuario {
    private Long idUsuario;
    private String nombre;
    private String email;

    public Usuario() {}

    public Usuario(Long idUsuario, String nombre, String email) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
    }

    public Long getId() { return idUsuario; }
    public void setId(Long id) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario + ", nombre='" + nombre + "', email='" + email + "'}";
    }
}
