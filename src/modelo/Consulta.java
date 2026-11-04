package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.EdadMinimaVacunaException;
import modelo.excepciones.PesoNoRegistradoException;

public class Consulta implements Serializable {
    private Turno turno;
    private LocalDate fecha;
    private String motivo;
    private String diagnostico;
    private String observaciones;
    private Double pesoDelDia;                 // null 
    private List<Tratamiento> tratamientos;
    private List<VacunaAplicada> vacunas;

    
    public Consulta(Turno turno, String motivo) {
        turno.marcarAtendido();                // lanza IllegalStateException si no corresponde
        this.turno = turno;
        this.fecha = turno.getFechaHora().toLocalDate();
        this.motivo = motivo;
        this.tratamientos = new ArrayList<>();
        this.vacunas = new ArrayList<>();
    }

    public void registrarPeso(double peso) {
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero.");
        }
        this.pesoDelDia = peso;
    }

    public void registrarDiagnostico(Persona autor, String diagnostico) {
        exigirVeterinario(autor);
        this.diagnostico = diagnostico;
    }

    public void indicarTratamiento(Persona autor, Tratamiento tratamiento)
            throws PesoNoRegistradoException {
        exigirVeterinario(autor);
        if (pesoDelDia == null) {
            throw new PesoNoRegistradoException(
                "Registrá el peso del animal antes de indicar un medicamento.");
        }
        tratamientos.add(tratamiento);
    }

    public void aplicarVacuna(Vacuna vacuna, String lote)
            throws EdadMinimaVacunaException {
        vacuna.validarAplicacion(turno.getMascota());
        vacunas.add(new VacunaAplicada(fecha, lote, vacuna));
    }

    private void exigirVeterinario(Persona autor) {
        if (!(autor instanceof Veterinario)) {
            throw new IllegalArgumentException(
                "Solo un veterinario puede registrar diagnósticos y tratamientos.");
        }
    }

    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Turno getTurno() { 
        return turno; }
    public LocalDate getFecha() { 
        return fecha; }

    public String getMotivo() { 
        return motivo; }

    public String getDiagnostico() { 
        return diagnostico; }

    public String getObservaciones() { 
        return observaciones; }

    public Double getPesoDelDia() { 
        return pesoDelDia; }

    public List<Tratamiento> getTratamientos() { 
        return Collections.unmodifiableList(tratamientos); }
        
    public List<VacunaAplicada> getVacunas() { 
        return Collections.unmodifiableList(vacunas); }
}
