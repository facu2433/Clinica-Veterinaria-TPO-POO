import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import modelo.*;
import modelo.excepciones.*;

/** Pruebas de turnos: agenda del veterinario, cancelación, reprogramación, urgencias y seña. */
public class PruebasAgenda {

    // Lunes 9/11/2026
    private static final LocalDateTime LUNES_10 = LocalDateTime.of(2026, 11, 9, 10, 0);

    public static void main(String[] args) {
        ejecutar();
        Prueba.terminar();
    }

    public static void ejecutar() {
        Prueba.titulo("Agenda y turnos");
        Prueba.correr("un turno dentro del horario se agenda", PruebasAgenda::turnoDentroDelHorario);
        Prueba.correr("un turno fuera del horario se rechaza", PruebasAgenda::turnoFueraDelHorario);
        Prueba.correr("dos turnos no pueden superponerse", PruebasAgenda::turnosSuperpuestos);
        Prueba.correr("cancelar libera el horario y tarde tiene cargo", PruebasAgenda::cancelarTurno);
        Prueba.correr("reprogramar un turno", PruebasAgenda::reprogramarTurno);
        Prueba.correr("una urgencia desplaza al turno común", PruebasAgenda::urgenciaDesplazaTurno);
        Prueba.correr("tres ausencias seguidas piden seña", PruebasAgenda::seniaPorAusencias);
    }

    private static Veterinario veterinario() {
        Veterinario v = new Veterinario("Laura", "Gómez", "30111222", "11-5555", "MP123", "Clínica general");
        v.agregarHorario(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        return v;
    }

    private static Perro perro() {
        return new Perro("Rex", 12.5, "Mestizo", "M", LocalDate.now().minusYears(2), "CHIP1", "mediano");
    }

    private static void turnoDentroDelHorario() throws Exception {
        Veterinario v = veterinario();
        Perro rex = perro();
        Turno t = new Turno(rex, v, LUNES_10, TipoAtencion.CONSULTA_GENERAL);
        v.agendarTurno(t);

        Prueba.afirmar(v.getAgenda().contains(t), "el turno tendría que estar en la agenda");
        Prueba.afirmar(t.getEstado() == Estado.PROGRAMADO, "el turno nuevo tendría que estar PROGRAMADO");
        Prueba.afirmar(t.getFin().equals(LUNES_10.plusMinutes(30)), "la consulta general dura 30 minutos");
    }

    private static void turnoFueraDelHorario() {
        Veterinario v = veterinario();
        Perro rex = perro();

        Prueba.esperar(TurnoSuperpuestoException.class,
                () -> v.agendarTurno(new Turno(rex, v, LocalDateTime.of(2026, 11, 9, 20, 0), TipoAtencion.CONSULTA_GENERAL)),
                "turno a las 20:00");
        Prueba.esperar(TurnoSuperpuestoException.class,
                () -> v.agendarTurno(new Turno(rex, v, LocalDateTime.of(2026, 11, 10, 10, 0), TipoAtencion.CONSULTA_GENERAL)),
                "turno un martes, que no atiende");
        Prueba.afirmar(v.getAgenda().isEmpty(), "los turnos rechazados no tendrían que quedar en la agenda");
    }

    private static void turnosSuperpuestos() throws Exception {
        Veterinario v = veterinario();
        Perro rex = perro();
        v.agendarTurno(new Turno(rex, v, LUNES_10, TipoAtencion.CONSULTA_GENERAL));

        Prueba.esperar(TurnoSuperpuestoException.class,
                () -> v.agendarTurno(new Turno(rex, v, LUNES_10, TipoAtencion.CONSULTA_GENERAL)),
                "mismo horario");
        Prueba.esperar(TurnoSuperpuestoException.class,
                () -> v.agendarTurno(new Turno(rex, v, LUNES_10.plusMinutes(15), TipoAtencion.CONSULTA_GENERAL)),
                "superposición parcial");

        // un turno que empieza justo cuando termina el otro sí entra
        v.agendarTurno(new Turno(rex, v, LUNES_10.plusMinutes(30), TipoAtencion.CONSULTA_GENERAL));
        Prueba.afirmar(v.getAgenda().size() == 2, "tendría que haber 2 turnos en la agenda");
    }

    private static void cancelarTurno() throws Exception {
        Veterinario v = veterinario();
        Perro rex = perro();
        Turno t = new Turno(rex, v, LUNES_10, TipoAtencion.CONSULTA_GENERAL);
        v.agendarTurno(t);

        boolean hayCargo = t.cancelar(LUNES_10.minusDays(1));
        Prueba.afirmar(!hayCargo, "cancelar con un día de aviso no tendría que tener cargo");
        Prueba.afirmar(t.getEstado() == Estado.CANCELADO, "el turno tendría que quedar CANCELADO");
        Prueba.afirmar(v.estaDisponible(LUNES_10, 30), "el horario tendría que quedar libre");

        Turno tarde = new Turno(rex, v, LUNES_10, TipoAtencion.CONSULTA_GENERAL);
        Prueba.afirmar(tarde.cancelar(LUNES_10.minusHours(1)), "cancelar 1 hora antes tendría que tener cargo");
        Turno justo = new Turno(rex, v, LUNES_10, TipoAtencion.CONSULTA_GENERAL);
        Prueba.afirmar(!justo.cancelar(LUNES_10.minusHours(2)), "cancelar 2 horas antes no tiene cargo");
    }

    private static void reprogramarTurno() throws Exception {
        Veterinario v = veterinario();
        Perro rex = perro();
        Turno t1 = new Turno(rex, v, LUNES_10, TipoAtencion.CONSULTA_GENERAL);
        Turno t2 = new Turno(rex, v, LUNES_10.plusHours(1), TipoAtencion.CONSULTA_GENERAL);
        v.agendarTurno(t1);
        v.agendarTurno(t2);

        v.reprogramarTurno(t1, LUNES_10.plusHours(3));
        Prueba.afirmar(t1.getFechaHora().equals(LUNES_10.plusHours(3)), "el turno tendría que tener el horario nuevo");
        Prueba.afirmar(v.estaDisponible(LUNES_10, 30), "el horario viejo tendría que quedar libre");

        Prueba.esperar(TurnoSuperpuestoException.class,
                () -> v.reprogramarTurno(t1, LUNES_10.plusHours(1)),
                "reprogramar sobre otro turno");
        Prueba.afirmar(t1.getFechaHora().equals(LUNES_10.plusHours(3)),
                "si se rechaza, el turno tendría que conservar su horario");

        t2.cancelar(LUNES_10.minusDays(1));
        Prueba.esperar(IllegalStateException.class, () -> v.reprogramarTurno(t2, LUNES_10.plusHours(5)),
                "reprogramar un turno cancelado");
    }

    private static void urgenciaDesplazaTurno() throws Exception {
        LocalDateTime nueve = LocalDateTime.of(2026, 11, 9, 9, 0);
        Veterinario v = veterinario();
        Perro rex = perro();
        Perro toto = new Perro("Toto", 8, "Mestizo", "M", LocalDate.now().minusYears(1), "CHIP2", "chico");

        Turno comun = new Turno(rex, v, nueve, TipoAtencion.CONSULTA_GENERAL);
        v.agendarTurno(comun);

        Turno urgencia = new Turno(toto, v, nueve, TipoAtencion.CONSULTA_GENERAL, true);
        List<Reprogramacion> movidos = v.agendarUrgencia(urgencia);

        Prueba.afirmar(v.getAgenda().contains(urgencia), "la urgencia tendría que estar en la agenda");
        Prueba.afirmar(movidos.size() == 1 && movidos.get(0).getTurno() == comun,
                "tendría que haberse movido solo el turno común");
        Prueba.afirmar(comun.getFechaHora().equals(nueve.plusMinutes(30)),
                "el turno común tendría que pasar al primer horario libre (9:30)");
        Prueba.afirmar(movidos.get(0).getFechaAnterior().equals(nueve), "la reprogramación recuerda el horario anterior");
    }

    private static void seniaPorAusencias() throws Exception {
        Veterinario v = veterinario();
        Duenio ana = new Duenio("Ana", "Pérez", "20111222", "11-1111", "ana@mail.com", null);
        Perro rex = perro();
        ana.agregarMascota(rex);

        for (int hora = 9; hora <= 10; hora++) {
            Turno t = new Turno(rex, v, LUNES_10.withHour(hora), TipoAtencion.CONSULTA_GENERAL);
            v.agendarTurno(t);
            t.marcarAusente();
        }
        Prueba.afirmar(!ana.requiereSenia(), "con 2 ausencias todavía no se pide seña");

        Turno tercero = new Turno(rex, v, LUNES_10.withHour(11), TipoAtencion.CONSULTA_GENERAL);
        v.agendarTurno(tercero);
        tercero.marcarAusente();
        Prueba.afirmar(ana.requiereSenia(), "con 3 ausencias seguidas se pide seña");
        Prueba.esperar(SeniaPendienteException.class, ana::verificarPuedePedirTurno,
                "pedir turno con la seña pendiente");

        ana.registrarSenia(10000, LUNES_10.toLocalDate());
        Prueba.afirmar(!ana.requiereSenia(), "con la seña paga ya no se pide");
        ana.verificarPuedePedirTurno();
    }
}
