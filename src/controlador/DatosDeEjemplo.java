package controlador;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import modelo.Duenio;
import modelo.Entorno;
import modelo.Especie;
import modelo.PlanSalud;
import modelo.Producto;
import modelo.TipoAtencion;
import modelo.Veterinario;

/**
 * Datos de prueba para la defensa y el README. Usa la misma API pública del
 * controlador que la consola, así pasa por las mismas validaciones y se guarda igual.
 */
public final class DatosDeEjemplo {

    private DatosDeEjemplo() {
    }

    public static void cargar(ControladorClinica c) throws IOException {
        if (c.tieneDatos()) {
            throw new IllegalStateException(
                "Ya hay datos cargados: los de ejemplo solo se pueden cargar en un sistema vacío.");
        }
        LocalDate hoy = LocalDate.now();

        // Precios de atención (supuesto: el enunciado no los define)
        c.definirPrecioAtencion(TipoAtencion.CONSULTA_GENERAL, 20000);
        c.definirPrecioAtencion(TipoAtencion.VACUNACION, 10000);
        c.definirPrecioAtencion(TipoAtencion.CIRUGIA, 150000);

        // Catálogo de vacunas
        c.registrarVacuna("Quíntuple", 2, 365, Arrays.asList(Especie.PERRO));
        c.registrarVacuna("Antirrábica", 4, 365, Arrays.asList(Especie.PERRO, Especie.GATO));
        c.registrarVacuna("Triple felina", 2, 365, Arrays.asList(Especie.GATO));
        c.registrarVacuna("Mixomatosis", 2, 180, Arrays.asList(Especie.CONEJO));

        // Veterinarios: Gómez de lunes a viernes, Díaz lunes, miércoles y viernes
        Veterinario gomez = c.registrarVeterinario("Laura", "Gómez", "30111222", "11-5555-0001", "MP-1001", "Clínica general");
        for (DayOfWeek dia : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                              DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            c.agregarHorario(gomez, dia, LocalTime.of(9, 0), LocalTime.of(18, 0));
        }
        Veterinario diaz = c.registrarVeterinario("Martín", "Díaz", "28999888", "11-5555-0002", "MP-1002", "Cirugía");
        for (DayOfWeek dia : new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}) {
            c.agregarHorario(diaz, dia, LocalTime.of(8, 0), LocalTime.of(14, 0));
        }

        // Gómez atiende las urgencias
        c.designarVeterinarioDeGuardia(gomez);

        // Dueños (Ana con plan de salud del 15 %) y mascotas
        Duenio ana = c.registrarDuenio("Ana", "Pérez", "20111222", "11-1111-0001", "ana@mail.com",
                new PlanSalud("Plan Plus", 15));
        Duenio bruno = c.registrarDuenio("Bruno", "Ruiz", "20333444", "11-1111-0002", "bruno@mail.com", null);
        c.registrarPerro(ana, "Rex", 22.5, "Labrador", "M", hoy.minusYears(3), "CHIP-0001", "grande");
        c.registrarGato(ana, "Misu", 4.2, "Siamés", "H", hoy.minusYears(1), Entorno.INTERIOR);
        c.registrarConejo(bruno, "Bugs", 2.1, "Enano", "M", hoy.minusMonths(8));
        c.registrarAve(bruno, "Piolín", 0.1, "Canario", "M", hoy.minusMonths(10), true);

        // Productos: Suero queda bajo el mínimo; Amoxicilina tiene un lote vencido
        Producto amoxicilina = c.registrarProducto("MED-001", "Amoxicilina", 1500, 5);
        c.agregarLote(amoxicilina, "L-001", 20, hoy.plusMonths(6));
        c.agregarLote(amoxicilina, "L-000", 5, hoy.minusDays(10));
        c.agregarProveedor(amoxicilina, "PROV-1", "Droguería Sur");
        Producto meloxicam = c.registrarProducto("MED-002", "Meloxicam", 2200, 5);
        c.agregarLote(meloxicam, "L-010", 8, hoy.plusMonths(3));
        Producto suero = c.registrarProducto("INS-001", "Suero fisiológico", 800, 10);
        c.agregarLote(suero, "L-020", 6, hoy.plusYears(1));
    }
}
