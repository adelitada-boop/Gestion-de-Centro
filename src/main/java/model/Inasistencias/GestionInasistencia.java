package model.Inasistencias;

import dao.InasistenciaDAOImpl;
import model.Docente.Docentes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class GestionInasistencia implements ABCM {
    private List<Inasistencias> coleccionInasistencias;
    private Map<String, Docentes> docentesRef;
    private Scanner scanner;
    private InasistenciaDAOImpl inasistenciaDAO; // Puente con PostgreSQL

    public GestionInasistencia(Map<String, Docentes> docentesRef) {
        this.inasistenciaDAO = new InasistenciaDAOImpl();
        this.docentesRef = docentesRef;
        this.scanner = new Scanner(System.in);
        cargarDesdeBaseDeDatos();
    }

    private void cargarDesdeBaseDeDatos() {
        this.coleccionInasistencias = inasistenciaDAO.listarTodas();
        if (this.coleccionInasistencias == null) {
            this.coleccionInasistencias = new ArrayList<>();
        }
    }

    public List<Inasistencias> getColeccionInasistencias() {
        cargarDesdeBaseDeDatos();
        return coleccionInasistencias;
    }

    @Override
    public void alta() {
        System.out.println("\n--- REGISTRO DE INASISTENCIA (ALTA) ---");
        System.out.print("Ingrese cédula del docente: ");
        String ci = scanner.nextLine().trim();
        if (!docentesRef.containsKey(ci)) {
            System.out.println("Error: No existe ningún docente con la C.I. " + ci);
            return;
        }
        Docentes docente = docentesRef.get(ci);


        System.out.print("Ingrese la cantidad de horas de clase no dictadas: ");
        int horas;
        try {
            horas = Integer.parseInt(scanner.nextLine().trim());
            if (horas <= 0) {
                System.out.println("Error: Las horas deben ser mayor a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Debe ingresar un número válido para las horas.");
            return;
        }
        System.out.println("\nSeleccione el motivo/artículo de inasistencia:");
        System.out.println("1. Art. 71 - Motivos personales");
        System.out.println("2. Art. 70.10 - Cursos oficiales");
        System.out.println("3. Art. 70.11 - Estudios terciarios");
        System.out.println("4. Art. 70.12 - Reunión o Exámenes");
        System.out.println("5. Aviso (A descuento)");
        System.out.println("6. Sin Aviso (A descuento doble)");
        System.out.print("Opción: ");
        Inasistencias.ArticuloInasistencia motivo;
        switch (scanner.nextLine().trim()) {
            case "1":
                motivo = Inasistencias.ArticuloInasistencia.ART_71;
                break;
            case "2":
                motivo = Inasistencias.ArticuloInasistencia.ART_70_10;
                break;
            case "3":
                motivo = Inasistencias.ArticuloInasistencia.ART_70_11;
                break;
            case "4":
                motivo = Inasistencias.ArticuloInasistencia.ART_70_12;
                break;
            case "5":
                motivo = Inasistencias.ArticuloInasistencia.AVISO;
                break;
            default:
                motivo = Inasistencias.ArticuloInasistencia.SIN_AVISO;
                break;
        }

        // --- Solicita fecha retroactiva o dejar la de hoy ---
        LocalDate fechaInasistencia = null;
        boolean fechaValida = false;

        while (!fechaValida) {
            System.out.print("Ingrese la fecha de la inasistencia (Formato AAAA-MM-DD) [Presione Enter para hoy]: ");
            String fechaStr = scanner.nextLine().trim();

            if (fechaStr.isEmpty()) {
                fechaInasistencia = LocalDate.now();
                fechaValida = true;
            } else {
                try {
                    fechaInasistencia = LocalDate.parse(fechaStr);
                    fechaValida = true;
                } catch (java.time.format.DateTimeParseException e) {
                    System.out.println("❌ Formato inválido. Use el formato AAAA-MM-DD (ej: 2026-09-15).");
                }
            }
        }

        final LocalDate fechaFiltro = fechaInasistencia;

        // Control Art. 71
        if (motivo == Inasistencias.ArticuloInasistencia.ART_71) {
            int topeArt71 = docente.calcularTopeAnualArt71(12);
            long art71Anual = coleccionInasistencias.stream()
                    .filter(f -> f.getCedulaDocente().equals(ci) &&
                            f.getMotivo() == Inasistencias.ArticuloInasistencia.ART_71 &&
                            f.getFecha().getYear() == fechaFiltro.getYear())
                    .count();
            if (art71Anual >= topeArt71) {
                System.out.println("\n[ALERTA] El docente alcanzó el tope anual para el Art. 71 (" + art71Anual + "/" + topeArt71 + ").");
                return;
            }
        }

        // Control Art. 70.11
        if (motivo == Inasistencias.ArticuloInasistencia.ART_70_11) {
            long art7011Mensual = coleccionInasistencias.stream()
                    .filter(f -> f.getCedulaDocente().equals(ci) &&
                            f.getMotivo() == Inasistencias.ArticuloInasistencia.ART_70_11 &&
                            f.getFecha().getYear() == fechaFiltro.getYear() &&
                            f.getFecha().getMonth() == fechaFiltro.getMonth())
                    .count();

            if (art7011Mensual >= 5) {
                System.out.println("\n[ALERTA] El docente alcanzó el tope mensual para el Art. 70.11 (5/5).");
                return;
            }

            long art7011Anual = coleccionInasistencias.stream()
                    .filter(f -> f.getCedulaDocente().equals(ci) &&
                            f.getMotivo() == Inasistencias.ArticuloInasistencia.ART_70_11 &&
                            f.getFecha().getYear() == fechaFiltro.getYear())
                    .count();

            if (art7011Anual >= 30) {
                System.out.println("\n[ALERTA] El docente alcanzó el tope anual para el Art. 70.11 (30/30).");
                return;
            }
        }

        Inasistencias nuevaInasistencia = new Inasistencias(ci, fechaInasistencia, horas, motivo);


        if (inasistenciaDAO.insertar(nuevaInasistencia)) {
            coleccionInasistencias.add(nuevaInasistencia);
            System.out.println("¡Inasistencia registrada con éxito en la BD para " + docente.getNombre() + " (" + horas + " hrs)!");
        } else {
            System.out.println("Error al registrar la inasistencia en la base de datos.");
        }
    }

    @Override
    public void baja() {
        System.out.println("\n--- ELIMINAR INASISTENCIA (BAJA) ---");
        System.out.print("Ingrese la cédula del docente de la inasistencia a eliminar: ");
        String ci = scanner.nextLine().trim();
        List<Inasistencias> delDocente = new ArrayList<>();
        for (Inasistencias f : coleccionInasistencias) {
            if (f.getCedulaDocente().equals(ci)) {
                delDocente.add(f);
            }
        }
        if (delDocente.isEmpty()) {
            System.out.println("No se encontraron inasistencias registradas para esa cédula.");
            return;
        }
        System.out.println("\nInasistencias encontradas:");
        for (int i = 0; i < delDocente.size(); i++) {
            System.out.println((i + 1) + ". " + delDocente.get(i));
        }
        System.out.print("Ingrese el número de la inasistencia que desea eliminar (0 para cancelar): ");
        try {
            int opcion = Integer.parseInt(scanner.nextLine().trim());
            if (opcion > 0 && opcion <= delDocente.size()) {
                Inasistencias aEliminar = delDocente.get(opcion - 1);

                if (inasistenciaDAO.eliminar(aEliminar.getCedulaDocente(), aEliminar.getFecha().toString())) {
                    coleccionInasistencias.remove(aEliminar);
                    System.out.println("Inasistencia eliminada correctamente de la base de datos.");
                } else {
                    System.out.println("Error al eliminar en la base de datos.");
                }
            } else {
                System.out.println("Operación cancelada.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Debe ingresar un número válido.");
        }
    }

    @Override
    public void modifica() {
        System.out.println("\n--- MODIFICAR INASISTENCIA ---");
        System.out.print("Ingrese la cédula del docente: ");
        String ci = scanner.nextLine().trim();
        List<Inasistencias> delDocente = new ArrayList<>();
        for (Inasistencias f : coleccionInasistencias) {
            if (f.getCedulaDocente().equals(ci)) {
                delDocente.add(f);
            }
        }
        if (delDocente.isEmpty()) {
            System.out.println("No hay inasistencias para esta cédula.");
            return;
        }
        System.out.println("\nInasistencias registradas:");
        for (int i = 0; i < delDocente.size(); i++) {
            System.out.println((i + 1) + ". " + delDocente.get(i));
        }
        System.out.print("Seleccione el número de inasistencia a modificar: ");
        try {
            int opcion = Integer.parseInt(scanner.nextLine().trim());
            if (opcion < 1 || opcion > delDocente.size()) {
                System.out.println("Opción inválida.");
                return;
            }
            Inasistencias faltaSeleccionada = delDocente.get(opcion - 1);

            System.out.print("Ingrese nuevas horas de falta (actuales: " + faltaSeleccionada.getHoras() + "): ");
            int nuevasHoras = Integer.parseInt(scanner.nextLine().trim());
            if (nuevasHoras <= 0) {
                System.out.println("Error: Las horas deben ser mayores a 0.");
                return;
            }

            System.out.println("\nSeleccione el nuevo motivo/artículo de inasistencia (actual: " + faltaSeleccionada.getMotivo() + "):");
            System.out.println("1. Art. 71 - Motivos personales");
            System.out.println("2. Art. 70.10 - Cursos oficiales");
            System.out.println("3. Art. 70.11 - Estudios terciarios");
            System.out.println("4. Art. 70.12 - Reunión o Exámenes");
            System.out.println("5. Aviso (A descuento)");
            System.out.println("6. Sin Aviso (A descuento doble)");
            System.out.print("Opción: ");

            Inasistencias.ArticuloInasistencia nuevoMotivo;
            switch (scanner.nextLine().trim()) {
                case "1":
                    nuevoMotivo = Inasistencias.ArticuloInasistencia.ART_71;
                    break;
                case "2":
                    nuevoMotivo = Inasistencias.ArticuloInasistencia.ART_70_10;
                    break;
                case "3":
                    nuevoMotivo = Inasistencias.ArticuloInasistencia.ART_70_11;
                    break;
                case "4":
                    nuevoMotivo = Inasistencias.ArticuloInasistencia.ART_70_12;
                    break;
                case "5":
                    nuevoMotivo = Inasistencias.ArticuloInasistencia.AVISO;
                    break;
                default:
                    nuevoMotivo = Inasistencias.ArticuloInasistencia.SIN_AVISO;
                    break;
            }

            // 3. Modificar en la base de datos (Llamada limpia sin asignatura)
            if (inasistenciaDAO.modificar(faltaSeleccionada.getCedulaDocente(), faltaSeleccionada.getFecha().toString(), nuevasHoras, nuevoMotivo.name())) {
                faltaSeleccionada.setHoras(nuevasHoras);
                faltaSeleccionada.setMotivo(nuevoMotivo);
                System.out.println("¡Inasistencia modificada con éxito (horas y causal) en la base de datos!");
            } else {
                System.out.println("Error al actualizar en la base de datos.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error en el formato del número ingresado.");
        }
    }

    @Override
    public void consulta() {
        cargarDesdeBaseDeDatos(); // Refrescamos desde PostgreSQL
        System.out.println("\n--- INASISTENCIAS INGRESADAS AL SISTEMA ---");
        System.out.print("Ingrese cédula del docente a consultar: ");
        String ci = scanner.nextLine().trim();
        if (!docentesRef.containsKey(ci)) {
            System.out.println("Error: No existe el docente con la C.I. " + ci);
            return;
        }
        Docentes docente = docentesRef.get(ci);
        System.out.println("\nHistorial de Inasistencias: " + docente.getNombre() + " (C.I.: " + ci + ")");
        System.out.println("----------------------------------------------------------------------");

        int contArt71 = 0, contArt70_10 = 0, contArt70_11Mensual = 0, contArt70_11Anual = 0;
        int contArt70_12 = 0, contAviso = 0, contSinAviso = 0;
        int totalHorasPerdidas = 0;
        LocalDate hoy = LocalDate.now();
        int contador = 1;

        for (Inasistencias falta : coleccionInasistencias) {
            if (falta.getCedulaDocente().equals(ci)) {
                System.out.println(contador + ". Fecha: " + falta.getFecha() +
                        " | Horas: " + falta.getHoras() +
                        " | Motivo: " + falta.getMotivo().getDescripcion());
                contador++;
                totalHorasPerdidas += falta.getHoras();
                switch (falta.getMotivo()) {
                    case ART_71:
                        if (falta.getFecha().getYear() == hoy.getYear()) contArt71++;
                        break;
                    case ART_70_10:
                        contArt70_10++;
                        break;
                    case ART_70_11:
                        if (falta.getFecha().getYear() == hoy.getYear()) {
                            contArt70_11Anual++;
                            if (falta.getFecha().getMonth() == hoy.getMonth()) contArt70_11Mensual++;
                        }
                        break;
                    case ART_70_12:
                        contArt70_12++;
                        break;
                    case AVISO:
                        contAviso++;
                        break;
                    case SIN_AVISO:
                        contSinAviso++;
                        break;
                }
            }
        }
        int totalFaltas = contador - 1;
        if (totalFaltas == 0) {
            System.out.println("El docente no registra inasistencias en el sistema.");
            return;
        }
        int topeArt71 = docente.calcularTopeAnualArt71(12);
        System.out.println("\n================ RESUMEN DE INASISTENCIAS ================");
        System.out.println("* Art. 71 (Asuntos personales): " + contArt71 + " / " + topeArt71);
        System.out.println("* Art. 70.11 (Estudios terciarios) Mes: " + contArt70_11Mensual + "/5 | Año: " + contArt70_11Anual + "/30");
        System.out.println("* Total Eventos: " + totalFaltas + " | Total Horas Perdidas: " + totalHorasPerdidas + " hrs");
        System.out.println("==========================================================");
    }
}
