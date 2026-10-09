package model.Docente;

import dao.DocenteDAOImpl;
import model.Asignatura.Asignatura;
import model.Inasistencias.Inasistencias;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class GestionDocentes<D> implements ABCM<D> {
    private Map<String, Docentes> docentes;
    private Scanner scanner;
    private DocenteDAOImpl docenteDAO; // Puente con PostgreSQL

    public GestionDocentes() {
        this.docenteDAO = new DocenteDAOImpl();
        this.scanner = new Scanner(System.in);
        cargarDesdeBaseDeDatos();
    }

    private void cargarDesdeBaseDeDatos() {
        this.docentes = docenteDAO.listarDocentes();
        if (this.docentes == null) {
            this.docentes = new TreeMap<>();
        }
    }

    public Map<String, Docentes> getDocentes() {
        return this.docentes;
    }

    // Método para la Opción 7 del menú
    public Map<String, Docentes> obtenerDocentesPorAsignatura(String nombreAsignatura) {
        return docenteDAO.listarPorAsignatura(nombreAsignatura);
    }

    public void asignarAsignaturaADocente(Set<String> nombresAsignaturas) {
        System.out.println("\n--- ASIGNACIÓN DE MATERIA A DOCENTE ---");
        System.out.print("Ingrese la cédula del docente: ");
        String ci = scanner.nextLine().trim();
        if (!docentes.containsKey(ci)) {
            System.out.println("Error: No existe ningún docente registrado con la C.I. " + ci);
            return;
        }
        System.out.print("Ingrese la asignatura a asignar: ");
        String nombreMateria = scanner.nextLine().trim();
        boolean existe = nombresAsignaturas.stream()
                .anyMatch(m -> m.equalsIgnoreCase(nombreMateria));
        if (!existe) {
            System.out.println("Error: La asignatura '" + nombreMateria + "' no está registrada en el sistema.");
            return;
        }

        // Guardamos en la base de datos
        if (docenteDAO.asignarAsignatura(ci, nombreMateria)) {
            docentes.get(ci).agregarAsignatura(new Asignatura(nombreMateria));
            System.out.println("¡Asignatura '" + nombreMateria + "' asignada con éxito al docente y guardada en la BD!");
        } else {
            System.out.println("Error al asociar la asignatura en la base de datos.");
        }
    }

    @Override
    public void alta() {
        System.out.println("\n--- ALTA DE NUEVO DOCENTE ---");
        System.out.print("Ingrese Cédula de Identidad: ");
        String ci = scanner.nextLine().trim();

        if (docentes.containsKey(ci)) {
            System.out.println("Error: Ya existe un docente registrado con esa cédula.");
            return;
        }

        System.out.print("Ingrese Nombre y Apellido: ");
        String nombre = scanner.nextLine().trim();
        System.out.print("Ingrese Carga Horaria: ");
        int cargaHoraria = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Ingrese Sueldo Base: ");
        double sueldoBase = Double.parseDouble(scanner.nextLine().trim());

        System.out.println("Seleccione el Tipo de Docente:");
        System.out.println("1. Efectivo");
        System.out.println("2. Interino");
        System.out.println("3. Suplente");
        System.out.print("Opción: ");
        String op = scanner.nextLine().trim();

        Docentes nuevoDocente = null;

        switch (op) {
            case "1":
                System.out.print("Año de ingreso al sistema: ");
                int anioEf = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Número de Acta: ");
                String nroActaEf = scanner.nextLine().trim();
                nuevoDocente = new DocenteEfectivo(ci, nombre, cargaHoraria, sueldoBase, anioEf, nroActaEf);
                break;
            case "2":
                System.out.print("Año de ingreso al sistema: ");
                int anioInt = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Número de Acta: ");
                String nroActaInt = scanner.nextLine().trim();
                nuevoDocente = new DocenteInterino(ci, nombre, cargaHoraria, sueldoBase, nroActaInt, anioInt);
                break;
            case "3":
                System.out.print("Año de ingreso al sistema: ");
                int anioSup = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Número de Acta: ");
                String nroActaSup = scanner.nextLine().trim();
                System.out.print("Cédula Titular Reemplazado: ");
                String ciTitular = scanner.nextLine().trim();

                // El orden exacto: cedula, nombre, carga, sueldo, acta, ciTitular (String), anioSup (int)
                nuevoDocente = new DocenteSuplente(ci, nombre, cargaHoraria, sueldoBase, nroActaSup, ciTitular, anioSup);
                break;
            default:
                System.out.println("Opción inválida. Registro cancelado.");
                return;
        }

        if (docenteDAO.insertar(nuevoDocente)) {
            docentes.put(ci, nuevoDocente);
            System.out.println("¡Docente registrado con éxito en la base de datos y en memoria!");
        } else {
            System.out.println("Error al registrar el docente en la base de datos.");
        }
    }

    @Override
    public void baja() {
        System.out.print("Ingrese cédula a eliminar: ");
        String ci = scanner.nextLine().trim();
        if (docenteDAO.eliminar(ci)) {
            docentes.remove(ci);
            System.out.println("Docente eliminado correctamente de la base de datos.");
        } else {
            System.out.println("Docente no encontrado o error al eliminar.");
        }
    }

    @Override
    public void modifica() {
        System.out.print("Ingrese cédula del docente a modificar: ");
        String ci = scanner.nextLine().trim();
        if (docentes.containsKey(ci)) {
            System.out.print("Ingrese nuevo Nombre y Apellido: ");
            String nuevoNombre = scanner.nextLine().trim();

            if (docenteDAO.modificar(ci, nuevoNombre)) {
                docentes.get(ci).setNombre(nuevoNombre);
                System.out.println("Datos actualizados correctamente en la base de datos.");
            } else {
                System.out.println("Error al actualizar en la base de datos.");
            }
        } else {
            System.out.println("Docente no encontrado.");
        }
    }

    @Override
    public void consulta() {
        cargarDesdeBaseDeDatos(); // Refrescamos desde PostgreSQL
        System.out.println("\n====== PLANTEL DE DOCENTES Y SUS ASIGNATURAS ======");
        if (docentes.isEmpty()) {
            System.out.println("No hay docentes registrados en el sistema.");
            return;
        }
        for (Docentes doc : docentes.values()) {
            System.out.println("\nCédula: " + doc.getCedula() + " | Nombre: " + doc.getNombre()
                    + " | Carga Horaria: " + doc.getCargaHoraria() + " hrs | Sueldo Base: $" + doc.getSueldoBase());
            if (doc.getAsignaturas().isEmpty()) {
                System.out.println("   [Sin asignaturas asignadas]");
            } else {
                System.out.print("   Asignaturas: ");
                for (Asignatura a : doc.getAsignaturas()) {
                    System.out.print("[" + a.getNombre() + "] ");
                }
                System.out.println();
            }
        }
    }

    public void calcularDescuentos(java.util.List<Inasistencias> listaFaltas) {
        System.out.println("\n====== INFORME DE DESCUENTOS E INASISTENCIAS ======");
        if (docentes.isEmpty()) {
            System.out.println("No hay docentes registrados en el sistema.");
            return;
        }
        for (Docentes d : docentes.values()) {
            int horasAviso = 0;
            int horasSinAviso = 0;
            if (listaFaltas != null) {
                for (Inasistencias falta : listaFaltas) {
                    if (falta.getCedulaDocente().equals(d.getCedula())) {
                        if (falta.getMotivo() == Inasistencias.ArticuloInasistencia.AVISO) {
                            horasAviso += falta.getHoras();
                        } else if (falta.getMotivo() == Inasistencias.ArticuloInasistencia.SIN_AVISO) {
                            horasSinAviso += falta.getHoras();
                        }
                    }
                }
            }

            double sueldoLiquido = d.calcularSueldoLiquido(horasAviso, horasSinAviso);
            double sueldoBase = d.getSueldoBase();
            double totalDescuento = sueldoBase - sueldoLiquido;
            System.out.println("Docente: " + d.getNombre() + " (C.I.: " + d.getCedula() + ")");
            System.out.println("  • Horas con aviso (AVISO): " + horasAviso + " hs");
            System.out.println("  • Horas sin aviso (SIN_AVISO): " + horasSinAviso + " hs");
            System.out.println("  • Sueldo Base Nominal: $" + String.format("%.2f", sueldoBase));
            System.out.println("  • Total Descontado: $" + String.format("%.2f", totalDescuento));
            System.out.println("  • Sueldo Líquido Final: $" + String.format("%.2f", sueldoLiquido));
            System.out.println("--------------------------------------------------");
        }
    }
}