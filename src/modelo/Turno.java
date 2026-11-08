package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Turno implements Serializable {
    private static final long serialVersionUID = 1L;

    // Cancelar con menos de estas horas de anticipación genera cargo.
    private static final int HORAS_CANCELACION_SIN_CARGO = 2;

    private Mascota mascota;
    private Veterinario veterinario;
    private LocalDateTime fechaHora;
    private TipoAtencion tipoAtencion;
    private boolean esUrgencia;
    private Estado estado;

    public Turno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora,
                 TipoAtencion tipoAtencion, boolean esUrgencia) {
        if (mascota == null || veterinario == null || fechaHora == null || tipoAtencion == null) {
            throw new IllegalArgumentException(
                "Mascota, veterinario, fecha y tipo de atención son obligatorios.");
        }
        this.mascota = mascota;
        this.veterinario = veterinario;
        this.fechaHora = fechaHora;
        this.tipoAtencion = tipoAtencion;
        this.esUrgencia = esUrgencia;
        this.estado = Estado.PROGRAMADO;   // todo turno nace programado
    }

    /** Turno común (no urgente). */
    public Turno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora,
                 TipoAtencion tipoAtencion) {
        this(mascota, veterinario, fechaHora, tipoAtencion, false);
    }

    // ---- Agenda -------------------------------------------------------------

    public int getDuracionMinutos() {
        return tipoAtencion.getDuracionMinutos();
    }

    public LocalDateTime getFin() {
        return fechaHora.plusMinutes(getDuracionMinutos());
    }

    /** Un turno cancelado o ausente libera el horario; programado y atendido lo ocupan. */
    public boolean ocupaHorario() {
        return estado == Estado.PROGRAMADO || estado == Estado.ATENDIDO;
    }

    /**
     * ¿Este turno se superpone con el intervalo [inicio, fin)?
     * Turnos pegados (uno termina justo cuando empieza el otro) NO se superponen.
     */
    public boolean seSuperpone(LocalDateTime inicio, LocalDateTime fin) {
        return fechaHora.isBefore(fin) && getFin().isAfter(inicio);
    }

    // ---- Cambios de estado: solo desde PROGRAMADO ----------------------------

    public void marcarAtendido() {
        exigirProgramado("marcar como atendido");
        estado = Estado.ATENDIDO;
    }

    public void marcarAusente() {
        exigirProgramado("marcar como ausente");
        estado = Estado.AUSENTE;
    }

    /**
     * Cancela el turno.
     *
     * @return true si corresponde cargo (se cancela con menos de 2 horas de anticipación).
     *         El monto (20 %) lo calcula la facturación con el precio real de la consulta.
     */
    public boolean cancelar(LocalDateTime ahora) {
        if (ahora == null) {
            throw new IllegalArgumentException("La fecha y hora actual son obligatorias.");
        }
        exigirProgramado("cancelar");
        estado = Estado.CANCELADO;
        // Sin cargo hasta 2 horas antes (inclusive); con menos de 2 horas, hay cargo.
        return ahora.plusHours(HORAS_CANCELACION_SIN_CARGO).isAfter(fechaHora);
    }

    /**
     * Cambia el horario de un turno programado. Package-private: la validación de horario
     * y superposición es del veterinario (Veterinario.reprogramarTurno / agendarUrgencia).
     */
    void reprogramar(LocalDateTime nuevaFechaHora) {
        if (nuevaFechaHora == null) {
            throw new IllegalArgumentException("La nueva fecha y hora son obligatorias.");
        }
        exigirProgramado("reprogramar");
        this.fechaHora = nuevaFechaHora;
    }

    private void exigirProgramado(String accion) {
        if (estado != Estado.PROGRAMADO) {
            throw new IllegalStateException(
                "No se puede " + accion + " un turno que está en estado " + estado + ".");
        }
    }

    // ---- Getters -------------------------------------------------------------

    public Mascota getMascota() {
        return mascota;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public TipoAtencion getTipoAtencion() {
        return tipoAtencion;
    }

    public boolean esUrgencia() {
        return esUrgencia;
    }

    public Estado getEstado() {
        return estado;
    }
}
