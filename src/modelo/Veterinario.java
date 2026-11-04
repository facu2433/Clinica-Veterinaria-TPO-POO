package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.TurnoSuperpuestoException;

public class Veterinario extends Persona {
    private String matricula;
    private String especialidad;
    private List<HorarioAtencion> horarios;
    private List<Turno> agenda;

    public Veterinario(String nombre, String apellido, String dni, String telefono,String matricula, String especialidad) {
        super(nombre, apellido, dni, telefono);
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.agenda = new ArrayList<>();
        this.horarios = new ArrayList<>();
    }

    public void agregarHorario(HorarioAtencion horario) {
        for (HorarioAtencion h : horarios) {
            if (h.getDia() == horario.getDia()) {
                throw new IllegalArgumentException(
                    "Ya existe un horario cargado para " + horario.getDia() + ".");
            }
        }
        horarios.add(horario);
    }

    public boolean atiendeEn(LocalDateTime inicio, int duracionMin) {
        for (HorarioAtencion h : horarios) {
            if (h.cubre(inicio, duracionMin)) {
                return true;
            }
        }
        return false;
    }
    public boolean estaDisponible(LocalDateTime inicio, int duracionMin) {
        if (!atiendeEn(inicio, duracionMin)) {
            return false;
        }
        LocalDateTime fin = inicio.plusMinutes(duracionMin);
        for (Turno t : agenda) {
            if (t.ocupaHorario() && t.seSuperpone(inicio, fin)) {
                return false;
            }
        }
        return true;
    }

    public void agendarTurno(Turno turno) throws TurnoSuperpuestoException {
        if (!estaDisponible(turno.getFechaHora(), turno.getDuracionMinutos())) {
            throw new TurnoSuperpuestoException(
                "El veterinario " + getApellido() + " no está disponible el "
                + turno.getFechaHora() + ".");
        }
        agenda.add(turno);
    }

    public List<Turno> getAgenda() {
        return Collections.unmodifiableList(agenda);
    }

    @Override
    public String descripcion() {
        return "Veterinario: " + super.descripcion()
                + " (Mat. " + matricula + " - " + especialidad + ")";
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