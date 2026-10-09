package model.Docente;

public class DocenteSuplente extends Docentes {
    private String cedulaTitularReemplazado;

    public DocenteSuplente(String cedula, String nombre, int cargaHoraria, double sueldoBase, String nroActa, String cedulaTitularReemplazado, int anioIngreso) {
        super(cedula, nombre, cargaHoraria, sueldoBase, nroActa, anioIngreso);
        setCedulaTitularReemplazado(cedulaTitularReemplazado);
    }

    public String getCedulaTitularReemplazado() {
        return cedulaTitularReemplazado;
    }

    public void setCedulaTitularReemplazado(String cedulaTitularReemplazado) {
        if (cedulaTitularReemplazado == null || cedulaTitularReemplazado.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar la C.I. del docente titular.");
        }
        this.cedulaTitularReemplazado = cedulaTitularReemplazado.trim();
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
        return (mesesTrabajados < 1) ? 0 : 5;
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
        return super.toString() + " | Tipo: Suplente | Supliendo a C.I.: " + cedulaTitularReemplazado;
    }
}