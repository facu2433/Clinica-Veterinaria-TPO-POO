package modelo;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class HorarioAtencion implements Serializable {
    private static final long serialVersionUID = 1L;

    private DayOfWeek dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public HorarioAtencion(DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
        if (dia == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Día y horas son obligatorios.");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                "La hora de fin debe ser posterior a la de inicio.");
        }
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /** ¿Este horario cubre el turno completo (desde inicio hasta inicio + duración)? */
    public boolean cubre(LocalDateTime inicio, int duracionMin) {
        if (inicio == null || duracionMin <= 0) {
            throw new IllegalArgumentException("Inicio y duración (mayor a cero) son obligatorios.");
        }
        if (inicio.getDayOfWeek() != dia) {
            return false;
        }
        LocalTime desde = inicio.toLocalTime();
        LocalTime hasta = desde.plusMinutes(duracionMin);

        // Si al sumar la duración se pasa de medianoche, LocalTime "da la vuelta":
        // en ese caso el turno no entra en el día.
        if (hasta.isBefore(desde)) {
            return false;
        }
        return !desde.isBefore(horaInicio) && !hasta.isAfter(horaFin);
    }

    public DayOfWeek getDia() {
        return dia;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }
}
