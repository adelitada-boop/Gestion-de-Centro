package model.Docente;

import model.Asignatura.Asignatura;

import java.util.ArrayList;
import java.util.List;

public abstract class Docentes {
    private String cedula;
    private String nombre;
    protected String nroActa;
    private int cargaHoraria;
    private double sueldoBase;
    private int anioIngreso;
    private List<Asignatura> asignaturas;

    public Docentes(String cedula, String nombre, int cargaHoraria, double sueldoBase, String nroActa, int anioIngreso) {
        setCedula(cedula);
        setNombre(nombre);
        setCargaHoraria(cargaHoraria);
        setSueldoBase(sueldoBase);
        setnroActa(nroActa);
        setAnioIngreso(anioIngreso);
        this.asignaturas = new ArrayList<>();
    }

    // Métodos abstractos
    protected abstract void setnroActa(String nroActa);
    public abstract int calcularTopeAnualArt71(int mesesTrabajados);
    public abstract double calcularSueldoLiquido(int horasAviso, int horasSinAviso);

    // Getters y Setters
    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula no puede estar vacía.");
        }
        this.cedula = cedula.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        if (cargaHoraria <= 0) {
            throw new IllegalArgumentException("La carga horaria debe ser mayor a 0.");
        }
        this.cargaHoraria = cargaHoraria;
    }

    public double getSueldoBase() {
        return sueldoBase;
    }

    public void setSueldoBase(double sueldoBase) {
        if (sueldoBase < 0) {
            throw new IllegalArgumentException("El sueldo base no puede ser negativo.");
        }
        this.sueldoBase = sueldoBase;
    }

    public String getNroActa() {
        return nroActa;
    }

    protected void setNroActaCampo(String nroActa) {
        this.nroActa = nroActa;
    }

    public int getAnioIngreso() {
        return anioIngreso;
    }

    public void setAnioIngreso(int anioIngreso) {
        if (anioIngreso < 1950 || anioIngreso > 2026) {
            throw new IllegalArgumentException("El año de ingreso ingresado no es válido.");
        }
        this.anioIngreso = anioIngreso;
    }

    public List<Asignatura> getAsignaturas() {
        return new ArrayList<>(asignaturas);
    }

    public void agregarAsignatura(Asignatura asignatura) {
        if (asignatura == null || asignatura.getNombre() == null) {
            throw new IllegalArgumentException("La asignatura a agregar es inválida.");
        }
        boolean yaExiste = asignaturas.stream()
                .anyMatch(a -> a.getNombre().equalsIgnoreCase(asignatura.getNombre()));
        if (yaExiste) {
            throw new IllegalArgumentException("El docente " + nombre + " ya tiene asignada la materia '" + asignatura.getNombre() + "'.");
        }
        this.asignaturas.add(asignatura);
    }

    @Override
    public String toString() {
        return "C.I.: " + cedula + " | Nombre: " + nombre + " | Carga Horaria: " + cargaHoraria + " hrs | Sueldo Base: $" + sueldoBase + " | Ingreso: " + anioIngreso + " | N° Acta: " + nroActa;
    }


}