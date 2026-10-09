package model.Docente;

public class DocenteInterino extends Docentes {

    public DocenteInterino(String cedula, String nombre, int cargaHoraria, double sueldoBase, String nroActa, int anioIngreso) {
        super(cedula, nombre, cargaHoraria, sueldoBase, nroActa,anioIngreso);
    }

    @Override
    protected void setnroActa(String nroActa) {
        if (nroActa == null || nroActa.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de acta no puede estar vacío.");
        }
        this.nroActa = nroActa.trim();
    }

    @Override
    public int calcularTopeAnualArt71(int mesesTrabajados) {
        // En interinos se suelen requerir meses mínimos o tope reducido
        return 3;
    }

    @Override
    public double calcularSueldoLiquido(int horasAviso, int horasSinAviso) {
        if (getCargaHoraria() <= 0) return getSueldoBase();

        double valorHora = getSueldoBase() / getCargaHoraria();
        double descuento = (horasAviso * valorHora) + (horasSinAviso * valorHora * 2);

        return Math.max(0, getSueldoBase() - descuento);
    }

    @Override
    public String toString() {
        return super.toString() + " | Tipo: Interino";
    }
}

