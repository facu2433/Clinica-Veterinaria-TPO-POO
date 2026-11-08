package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Registro de un turno que cambió de horario (por una urgencia o a pedido). Inmutable. */
public class Reprogramacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Turno turno;
    private final LocalDateTime fechaAnterior;
    private final LocalDateTime fechaNueva;

    Reprogramacion(Turno turno, LocalDateTime fechaAnterior, LocalDateTime fechaNueva) {
        this.turno = turno;
        this.fechaAnterior = fechaAnterior;
        this.fechaNueva = fechaNueva;
    }

    public Turno getTurno() {
        return turno;
    }

    public LocalDateTime getFechaAnterior() {
        return fechaAnterior;
    }

    public LocalDateTime getFechaNueva() {
        return fechaNueva;
    }

    /** Texto de la notificación que recibe el dueño cuando una urgencia desplaza su turno. */
    public String mensajeParaElDuenio() {
        return "El turno de " + turno.getMascota().getNombre() + " con "
                + turno.getVeterinario().getApellido() + " del " + FORMATO.format(fechaAnterior)
                + " fue reprogramado para el " + FORMATO.format(fechaNueva)
                + " porque la clínica atendió una urgencia.";
    }
}
