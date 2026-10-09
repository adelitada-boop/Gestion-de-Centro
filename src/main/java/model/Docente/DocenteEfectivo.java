package model.Docente;

public class DocenteEfectivo extends Docentes {
    private String nroActa;

    public DocenteEfectivo(String cedula, String nombre, int cargaHoraria, double sueldoBase, int anioIngreso, String nroActa) {
        super(cedula, nombre, cargaHoraria, sueldoBase, nroActa, anioIngreso);
        this.nroActa = nroActa;
    }

    public String getNroActa() {
        return nroActa;
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
        return 5; // Tiene derecho pleno (5 días) desde el inicio del año lectivo
    }

    @Override
    public double calcularSueldoLiquido(int horasAviso, int horasSinAviso) {
        if (getCargaHoraria() <= 0) return getSueldoBase();
        // Valor de la hora docente según sueldo base y carga horaria
        double valorHora = getSueldoBase() / getCargaHoraria();
        // Descuento simple por horas con aviso + Descuento doble por horas sin aviso
        double descuento = (horasAviso * valorHora) + (horasSinAviso * valorHora * 2);
        return Math.max(0, getSueldoBase() - descuento);
    }

    @Override
    public String toString() {
        return super.toString() + " | Tipo: Efectivo | Concurso: " + getAnioIngreso() + " | Acta: " + getNroActa();
    }
}