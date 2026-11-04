package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Turno implements Serializable {
    private LocalDateTime fechaHora;
    private TipoAtencion tipoAtencion;
    private Estado estado;
    private boolean esUrgencia;
    private Mascota mascota;
    private Veterinario veterinario;

    public Turno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora,
                 TipoAtencion tipoAtencion, boolean esUrgencia) {
        if (mascota == null || veterinario == null
                || fechaHora == null || tipoAtencion == null) {
            throw new IllegalArgumentException("Todos los datos del turno son obligatorios.");
        }
        this.mascota = mascota;
        this.veterinario = veterinario;
        this.fechaHora = fechaHora;
        this.tipoAtencion = tipoAtencion;
        this.esUrgencia = esUrgencia;
        this.estado = Estado.PROGRAMADO;
    }

    public int getDuracionMinutos() {
        return tipoAtencion.getDuracionMinutos();
    }

    public LocalDateTime getFin() {
        return fechaHora.plusMinutes(getDuracionMinutos());
    }

    
    public boolean ocupaHorario() {
        return estado == Estado.PROGRAMADO || estado == Estado.ATENDIDO;
    }

    
    public boolean seSuperpone(LocalDateTime otroInicio, LocalDateTime otroFin) {
        return fechaHora.isBefore(otroFin) && otroInicio.isBefore(getFin());
    }

   
    public boolean cancelar(LocalDateTime ahora) {
        if (estado != Estado.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede cancelar un turno programado.");
        }
        boolean conCargo = ahora.isAfter(fechaHora.minusHours(2));
        estado = Estado.CANCELADO;
        return conCargo;
    }

    public void marcarAtendido() {
        if (estado != Estado.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede atender un turno programado.");
        }
        estado = Estado.ATENDIDO;
    }

    public void marcarAusente() {
        if (estado != Estado.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede marcar ausente un turno programado.");
        }
        estado = Estado.AUSENTE;
    }

    public LocalDateTime getFechaHora() { 
        return fechaHora; }

    public TipoAtencion getTipoAtencion() { 
        return tipoAtencion; }

    public Estado getEstado() { 
        return estado; }
        
    public boolean esUrgencia() { 
        return esUrgencia; }

    public Mascota getMascota() { 
        return mascota; }

    public Veterinario getVeterinario() { 
        return veterinario; }
}
