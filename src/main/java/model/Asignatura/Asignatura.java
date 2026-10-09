package model.Asignatura;

import java.util.Objects;
public class Asignatura {
    private String nombre;
    public Asignatura(String nombre) {
        setNombre(nombre);
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la asignatura no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asignatura queAsignatura = (Asignatura) o;
        return Objects.equals(nombre.toLowerCase(), queAsignatura.nombre.toLowerCase());
    }
    @Override
    public int hashCode() {
        return Objects.hash(nombre.toLowerCase());
    }
    @Override
    public String toString() {
        return nombre;
    }
}
