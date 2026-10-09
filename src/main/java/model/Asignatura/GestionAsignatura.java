package model.Asignatura;

import dao.AsignaturaDAOImpl;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class GestionAsignatura implements ABCM<Asignatura> {
    private Set<Asignatura> coleccionAsignaturas;
    private Scanner scanner;
    private AsignaturaDAOImpl asignaturaDAO; // Crea puente con PostgreSQL

    public GestionAsignatura() {
        this.asignaturaDAO = new AsignaturaDAOImpl();
        this.coleccionAsignaturas = new java.util.HashSet<>();
        this.scanner = new Scanner(System.in);

        cargarDesdeBaseDeDatos();
    }

    private void cargarDesdeBaseDeDatos() {
        List<Asignatura> listaBD = asignaturaDAO.listarTodas();
        coleccionAsignaturas.clear();
        coleccionAsignaturas.addAll(listaBD);
    }

    public Set<Asignatura> getColeccionAsignatura() {
        return coleccionAsignaturas;
    }

    public Set<String> getNombresAsignaturas() {
        Set<String> nombres = new java.util.HashSet<>();
        for (Asignatura a : coleccionAsignaturas) {
            nombres.add(a.getNombre());
        }
        return nombres;
    }

    @Override
    public void alta() {
        System.out.print("Ingrese el nombre de la nueva asignatura: ");
        String nom = scanner.nextLine().trim();
        try {
            Asignatura nueva = new Asignatura(nom);

            if (asignaturaDAO.insertar(nueva)) {
                coleccionAsignaturas.add(nueva);
                System.out.println("Asignatura '" + nueva.getNombre() + "' registrada con éxito en la base de datos.");
            } else {
                System.out.println("La asignatura ya se encuentra registrada o hubo un error en la BD.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void baja() {
        System.out.print("Ingrese el nombre de la asignatura a eliminar: ");
        String nom = scanner.nextLine().trim();
        try {
            Asignatura busqueda = new Asignatura(nom);

            // Eliminamos de la base de datos a través del DAO
            if (asignaturaDAO.eliminar(nom)) {
                coleccionAsignaturas.remove(busqueda);
                System.out.println("Asignatura '" + nom + "' eliminada de la base de datos.");
            } else {
                System.out.println("No se encontró la asignatura ingresada en la base de datos.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void consulta() {
        cargarDesdeBaseDeDatos();

        System.out.println("\n--- Lista de Asignaturas Registradas ---");
        if (coleccionAsignaturas.isEmpty()) {
            System.out.println("No hay asignaturas registradas.");
        } else {
            for (Asignatura asig : coleccionAsignaturas) {
                System.out.println("- " + asig.getNombre());
            }
        }
    }

    @Override
    public void modifica() {
        System.out.print("Ingrese el nombre de la asignatura a modificar: ");
        String nomViejo = scanner.nextLine().trim();
        try {
            Asignatura asigVieja = new Asignatura(nomViejo);

            if (coleccionAsignaturas.contains(asigVieja)) {
                System.out.print("Ingrese el nuevo nombre para la asignatura: ");
                String nomNuevo = scanner.nextLine().trim();
                Asignatura asigNueva = new Asignatura(nomNuevo);

                if (asignaturaDAO.modificar(nomViejo, asigNueva.getNombre())) {
                    coleccionAsignaturas.remove(asigVieja);
                    coleccionAsignaturas.add(asigNueva);
                    System.out.println("Asignatura modificada correctamente.");
                } else {
                    System.out.println("Error al modificar en la base de datos.");
                }
            } else {
                System.out.println("La asignatura a modificar no existe.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}