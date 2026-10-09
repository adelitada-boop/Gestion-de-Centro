package model.Inasistencias;

import java.time.LocalDate;

public class Inasistencias {
    public enum ArticuloInasistencia {
        ART_71("Art. 71 - Motivos personales"),
        ART_70_10("Art. 70.10 - Cursos oficiales"),
        ART_70_11("Art. 70.11 - Estudios terciarios"),
        ART_70_12("Art. 70.12 - Reunión o Exámenes"),
        AVISO("Aviso (A descuento)"),
        SIN_AVISO("Sin Aviso (A descuento doble)");

        private final String descripcion;

        ArticuloInasistencia(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private String cedulaDocente;
    private LocalDate fecha;
    private int horas;
    private ArticuloInasistencia motivo;


    public Inasistencias(String cedulaDocente, LocalDate fecha, int horas, ArticuloInasistencia motivo) {
        setCedulaDocente(cedulaDocente);
        setFecha(fecha);
        setHoras(horas);
        setMotivo(motivo);
    }

    public String getCedulaDocente() {
        return cedulaDocente;
    }

    public void setCedulaDocente(String cedulaDocente) {
        if (cedulaDocente == null || cedulaDocente.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula del docente no puede estar vacía.");
        }
        this.cedulaDocente = cedulaDocente.trim();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        this.fecha = fecha;
    }

    public int getHoras() {
        return horas;
    }

    public void setHoras(int horas) {
        if (horas <= 0) {
            throw new IllegalArgumentException("Las horas de inasistencia deben ser mayores a 0.");
        }
        this.horas = horas;
    }

    public ArticuloInasistencia getMotivo() {
        return motivo;
    }

    public void setMotivo(ArticuloInasistencia motivo) {
        if (motivo == null) {
            throw new IllegalArgumentException("El motivo o artículo de la inasistencia es obligatorio.");
        }
        this.motivo = motivo;
    }

    @Override
    public String toString() {
        return "Inasistencia [CI: " + cedulaDocente +
                " | Fecha: " + fecha +
                " | Horas: " + horas +
                " | Motivo: " + (motivo != null ? motivo.getDescripcion() : "N/A") + "]";
    }
}