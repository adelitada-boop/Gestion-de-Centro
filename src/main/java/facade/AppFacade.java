package facade;

import model.Docente.Docentes;
import model.Inasistencias.GestionInasistencia;
import model.Asignatura.GestionAsignatura;
import model.Docente.GestionDocentes;

import java.util.Map;
import java.util.Set;

public class AppFacade {
    private static AppFacade instancia;
    private GestionAsignatura gestionAsignatura;
    private GestionDocentes gestionDocentes;
    private GestionInasistencia gestionInasistencia;

    private AppFacade() {
        this.gestionAsignatura = new GestionAsignatura();
        this.gestionDocentes = new GestionDocentes();
        // Pasamos el mapa de docentes que administra gestionDocentes
        this.gestionInasistencia = new GestionInasistencia(gestionDocentes.getDocentes());
    }

    public static AppFacade getInstancia() {
        if (instancia == null) {
            instancia = new AppFacade();
        }
        return instancia;
    }

    // --- Métodos de Asignaturas ---
    public void altaAsignatura() { gestionAsignatura.alta(); }
    public void bajaAsignatura() { gestionAsignatura.baja(); }
    public void consultaAsignaturas() { gestionAsignatura.consulta(); }
    public void modificarAsignatura() { gestionAsignatura.modifica(); }
    public Set<String> obtenerNombresAsignaturas() {
        return gestionAsignatura.getNombresAsignaturas();
    }

    // --- Métodos de Docentes ---
    public void altaDocente() { gestionDocentes.alta(); }
    public void bajaDocente() { gestionDocentes.baja(); }
    public void consultaDocentes() { gestionDocentes.consulta(); }
    public void modificarDocente() { gestionDocentes.modifica(); }

    public void asignarAsignaturaADocente() {
        gestionDocentes.asignarAsignaturaADocente(gestionAsignatura.getNombresAsignaturas());
    }

    // Docente por asignatura
    public Map<String, Docentes> obtenerDocentesPorAsignatura(String nombreAsignatura) {
        return gestionDocentes.obtenerDocentesPorAsignatura(nombreAsignatura);
    }

    // --- Métodos de Inasistencias ---
    public void altaInasistencia() { gestionInasistencia.alta(); }
    public void bajaInasistencia() { gestionInasistencia.baja(); }
    public void consultaInasistencias() { gestionInasistencia.consulta(); }
    public void modificarInasistencia() { gestionInasistencia.modifica(); }

    public void calcularDescuentosDocentes() {
        gestionDocentes.calcularDescuentos(gestionInasistencia.getColeccionInasistencias());
    }
}