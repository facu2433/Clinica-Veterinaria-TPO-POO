package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.excepciones.TurnoSuperpuestoException;

public class Veterinario extends Persona {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Cuántos días hacia adelante se busca un horario libre (para urgencias y turnos desplazados).
    private static final int DIAS_DE_BUSQUEDA = 60;

    private String matricula;
    private String especialidad;
    private List<HorarioAtencion> horarios;
    private List<Turno> agenda;
    private boolean deGuardia;

    public Veterinario(String nombre, String apellido, String dni, String telefono,
                       String matricula, String especialidad) {
        super(nombre, apellido, dni, telefono);
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.agenda = new ArrayList<>();
        this.horarios = new ArrayList<>();
    }

    /** Un solo horario por día de la semana (supuesto 6 de la Parte A). */
    public void agregarHorario(HorarioAtencion horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario es obligatorio.");
        }
        for (HorarioAtencion h : horarios) {
            if (h.getDia() == horario.getDia()) {
                throw new IllegalArgumentException(
                    "Ya existe un horario cargado para " + horario.getDia() + ".");
            }
        }
        horarios.add(horario);
    }

    /** ¿Algún horario de atención cubre el turno completo? */
    public boolean atiendeEn(LocalDateTime inicio, int duracionMin) {
        for (HorarioAtencion h : horarios) {
            if (h.cubre(inicio, duracionMin)) {
                return true;
            }
        }
        return false;
    }

    /** Dentro de su horario y sin turnos superpuestos (los cancelados/ausentes no cuentan). */
    public boolean estaDisponible(LocalDateTime fechaHora, int duracionMinutos) {
        if (!atiendeEn(fechaHora, duracionMinutos)) {
            return false;
        }
        return !haySuperposicion(fechaHora, fechaHora.plusMinutes(duracionMinutos), null);
    }

    /** ¿Algún turno que ocupa horario (salvo "ignorado") se pisa con [inicio, fin)? */
    private boolean haySuperposicion(LocalDateTime inicio, LocalDateTime fin, Turno ignorado) {
        for (Turno t : agenda) {
            if (t != ignorado && t.ocupaHorario() && t.seSuperpone(inicio, fin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Forma normal de agregar un turno a la agenda. Valida horario y superposición.
     * Patrón Experto: el veterinario conoce su agenda, por eso él valida.
     */
    public void agendarTurno(Turno turno) throws TurnoSuperpuestoException {
        if (turno == null) {
            throw new IllegalArgumentException("El turno es obligatorio.");
        }
        if (turno.getVeterinario() != this) {
            throw new IllegalArgumentException("El turno pertenece a otro veterinario.");
        }
        if (!atiendeEn(turno.getFechaHora(), turno.getDuracionMinutos())) {
            throw new TurnoSuperpuestoException(
                "El veterinario " + getApellido() + " no atiende en el horario pedido ("
                + FORMATO.format(turno.getFechaHora()) + ").");
        }
        if (!estaDisponible(turno.getFechaHora(), turno.getDuracionMinutos())) {
            throw new TurnoSuperpuestoException(
                "El veterinario " + getApellido() + " ya tiene un turno superpuesto el "
                + FORMATO.format(turno.getFechaHora()) + ".");
        }
        incorporar(turno);
    }

    private void incorporar(Turno turno) {
        agenda.add(turno);
        turno.getMascota().registrarTurno(turno);
    }

    // ---- Reprogramación ------------------------------------------------------

    /**
     * Mueve un turno programado a otro horario del mismo veterinario. Valida lo mismo que al
     * asignar (horario de atención y superposición), sin contar el propio turno. El horario
     * anterior queda libre.
     */
    public void reprogramarTurno(Turno turno, LocalDateTime nuevaFechaHora) throws TurnoSuperpuestoException {
        if (turno == null || nuevaFechaHora == null) {
            throw new IllegalArgumentException("El turno y la nueva fecha y hora son obligatorios.");
        }
        if (!agenda.contains(turno)) {
            throw new IllegalArgumentException("El turno no pertenece a la agenda de " + getApellido() + ".");
        }
        if (turno.getEstado() != Estado.PROGRAMADO) {
            throw new IllegalStateException(
                "No se puede reprogramar un turno que está en estado " + turno.getEstado() + ".");
        }
        if (!atiendeEn(nuevaFechaHora, turno.getDuracionMinutos())) {
            throw new TurnoSuperpuestoException(
                "El veterinario " + getApellido() + " no atiende en el horario pedido ("
                + FORMATO.format(nuevaFechaHora) + ").");
        }
        if (haySuperposicion(nuevaFechaHora, nuevaFechaHora.plusMinutes(turno.getDuracionMinutos()), turno)) {
            throw new TurnoSuperpuestoException(
                "El veterinario " + getApellido() + " ya tiene un turno superpuesto el "
                + FORMATO.format(nuevaFechaHora) + ".");
        }
        turno.reprogramar(nuevaFechaHora);
    }

    // ---- Urgencias -----------------------------------------------------------

    /**
     * Primer horario, desde "ahora", en que una urgencia de esta duración entra en el horario de
     * atención. Los turnos programados comunes NO lo bloquean (la urgencia los desplaza); sí lo
     * bloquean los turnos ya atendidos y las otras urgencias.
     */
    public LocalDateTime primerHorarioParaUrgencia(LocalDateTime ahora, int duracionMinutos) {
        if (ahora == null || duracionMinutos <= 0) {
            throw new IllegalArgumentException("La fecha actual y la duración (mayor a cero) son obligatorias.");
        }
        List<Intervalo> bloqueados = new ArrayList<>();
        for (Turno t : agenda) {
            boolean bloquea = t.getEstado() == Estado.ATENDIDO
                    || (t.getEstado() == Estado.PROGRAMADO && t.esUrgencia());
            if (bloquea) {
                bloqueados.add(new Intervalo(t.getFechaHora(), t.getFin()));
            }
        }
        return buscarHorarioLibre(ahora, duracionMinutos, bloqueados);
    }

    /**
     * Agenda una urgencia en su horario. Si ese horario está ocupado por turnos comunes, los
     * desplaza: cada uno pasa al primer horario libre desde que termina la urgencia.
     * Es todo o nada: si no hay lugar para reprogramar alguno, no se modifica nada.
     *
     * @return los turnos que cambiaron de horario (para avisar a sus dueños)
     */
    public List<Reprogramacion> agendarUrgencia(Turno urgencia) throws TurnoSuperpuestoException {
        if (urgencia == null) {
            throw new IllegalArgumentException("El turno es obligatorio.");
        }
        if (urgencia.getVeterinario() != this) {
            throw new IllegalArgumentException("El turno pertenece a otro veterinario.");
        }
        if (!urgencia.esUrgencia()) {
            throw new IllegalArgumentException("El turno no es una urgencia.");
        }
        LocalDateTime inicio = urgencia.getFechaHora();
        LocalDateTime fin = urgencia.getFin();
        if (!atiendeEn(inicio, urgencia.getDuracionMinutos())) {
            throw new TurnoSuperpuestoException("El veterinario " + getApellido()
                + " no atiende en el horario pedido (" + FORMATO.format(inicio) + ").");
        }

        // 1) Qué turnos pisa la urgencia: los comunes se desplazan, los demás la impiden.
        List<Turno> desplazados = new ArrayList<>();
        for (Turno t : agenda) {
            if (t.ocupaHorario() && t.seSuperpone(inicio, fin)) {
                if (t.getEstado() == Estado.PROGRAMADO && !t.esUrgencia()) {
                    desplazados.add(t);
                } else {
                    throw new TurnoSuperpuestoException("El veterinario " + getApellido()
                        + " ya tiene una urgencia o una consulta atendida el " + FORMATO.format(t.getFechaHora()) + ".");
                }
            }
        }
        desplazados.sort(Comparator.comparing(Turno::getFechaHora));

        // 2) Planificar el nuevo horario de cada desplazado sin tocar nada todavía.
        List<Intervalo> ocupados = new ArrayList<>();
        for (Turno t : agenda) {
            if (t.ocupaHorario() && !desplazados.contains(t)) {
                ocupados.add(new Intervalo(t.getFechaHora(), t.getFin()));
            }
        }
        ocupados.add(new Intervalo(inicio, fin));
        Map<Turno, LocalDateTime> nuevosHorarios = new LinkedHashMap<>();
        for (Turno t : desplazados) {
            LocalDateTime nuevo = buscarHorarioLibre(fin, t.getDuracionMinutos(), ocupados);
            nuevosHorarios.put(t, nuevo);
            ocupados.add(new Intervalo(nuevo, nuevo.plusMinutes(t.getDuracionMinutos())));
        }

        // 3) Recién ahora se modifica la agenda.
        incorporar(urgencia);
        List<Reprogramacion> resultado = new ArrayList<>();
        for (Map.Entry<Turno, LocalDateTime> e : nuevosHorarios.entrySet()) {
            LocalDateTime anterior = e.getKey().getFechaHora();
            e.getKey().reprogramar(e.getValue());
            resultado.add(new Reprogramacion(e.getKey(), anterior, e.getValue()));
        }
        return resultado;
    }

    /**
     * Primer inicio, a partir de "desde", en que entra un turno de esa duración dentro del horario
     * de atención y sin pisar los intervalos ocupados. Si no hay lugar en los próximos 60 días
     * lanza IllegalStateException.
     */
    private LocalDateTime buscarHorarioLibre(LocalDateTime desde, int duracionMinutos, List<Intervalo> ocupados) {
        LocalDateTime candidato = desde.withSecond(0).withNano(0);
        LocalDateTime limite = desde.plusDays(DIAS_DE_BUSQUEDA);
        while (candidato.isBefore(limite)) {
            HorarioAtencion horario = horarioDelDia(candidato);
            if (horario == null) {
                candidato = candidato.toLocalDate().plusDays(1).atStartOfDay();
                continue;
            }
            LocalDateTime inicioDelDia = candidato.toLocalDate().atTime(horario.getHoraInicio());
            LocalDateTime finDelDia = candidato.toLocalDate().atTime(horario.getHoraFin());
            if (candidato.isBefore(inicioDelDia)) {
                candidato = inicioDelDia;
            }
            if (candidato.plusMinutes(duracionMinutos).isAfter(finDelDia)) {
                candidato = candidato.toLocalDate().plusDays(1).atStartOfDay();
                continue;
            }
            LocalDateTime finDelConflicto = null;
            for (Intervalo i : ocupados) {
                if (i.seSuperpone(candidato, candidato.plusMinutes(duracionMinutos))
                        && (finDelConflicto == null || i.fin.isAfter(finDelConflicto))) {
                    finDelConflicto = i.fin;
                }
            }
            if (finDelConflicto == null) {
                return candidato;
            }
            candidato = finDelConflicto;   // saltar al final del turno que molesta
        }
        throw new IllegalStateException("El veterinario " + getApellido()
            + " no tiene horarios libres en los próximos " + DIAS_DE_BUSQUEDA + " días.");
    }

    private HorarioAtencion horarioDelDia(LocalDateTime fecha) {
        for (HorarioAtencion h : horarios) {
            if (h.getDia() == fecha.getDayOfWeek()) {
                return h;
            }
        }
        return null;
    }

    /** Tramo de tiempo ocupado [inicio, fin). Los tramos pegados no se superponen. */
    private static class Intervalo {
        private final LocalDateTime inicio;
        private final LocalDateTime fin;

        Intervalo(LocalDateTime inicio, LocalDateTime fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        boolean seSuperpone(LocalDateTime otroInicio, LocalDateTime otroFin) {
            return inicio.isBefore(otroFin) && fin.isAfter(otroInicio);
        }
    }

    // ---- Guardia -------------------------------------------------------------

    /** Un veterinario de guardia atiende las urgencias. Solo debe haber uno (lo controla el controlador). */
    public boolean estaDeGuardia() {
        return deGuardia;
    }

    public void setDeGuardia(boolean deGuardia) {
        this.deGuardia = deGuardia;
    }

    /** Lista de solo lectura: para agregar hay que usar agendarTurno. */
    public List<Turno> getAgenda() {
        return Collections.unmodifiableList(agenda);
    }

    @Override
    public String descripcion() {
        return "Veterinario: \n" + super.descripcion()
                + "\nMatrícula: " + matricula + "\nEspecialidad: " + especialidad
                + (deGuardia ? "\nDe guardia: sí" : "");
    }

    public String getMatricula() {
        return matricula;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public List<HorarioAtencion> getHorarios() {
        return Collections.unmodifiableList(horarios);
    }
}
