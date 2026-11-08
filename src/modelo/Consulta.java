package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.EdadMinimaVacunaException;
import modelo.excepciones.PesoNoRegistradoException;

public class Consulta implements Serializable {
    private static final long serialVersionUID = 1L;

    private Turno turno;
    private LocalDate fecha;
    private String motivo;
    private String diagnostico;
    private String observaciones;
    private Double pesoDelDia;                 // null mientras no se registre
    private List<Tratamiento> tratamientos;
    private List<VacunaAplicada> vacunas;
    private boolean cerrada;                   // true una vez confirmada (facturada): ya no se edita

    /**
     * Package-private a propósito: las consultas se crean únicamente desde
     * HistoriaClinica.registrarConsulta(...), así nunca quedan fuera de la historia.
     * Lanza IllegalStateException si el turno no está PROGRAMADO.
     */
    Consulta(Turno turno, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la consulta es obligatorio.");
        }
        turno.marcarAtendido();
        this.turno = turno;
        this.fecha = turno.getFechaHora().toLocalDate();
        this.motivo = motivo;
        this.tratamientos = new ArrayList<>();
        this.vacunas = new ArrayList<>();
    }

    /** Registra el peso del día y actualiza el peso actual de la mascota. */
    public void registrarPeso(double peso) {
        exigirAbierta();
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero.");
        }
        this.pesoDelDia = peso;
        turno.getMascota().actualizarPeso(peso);
    }

    public void registrarDiagnostico(Persona autor, String diagnostico) {
        exigirAbierta();
        exigirVeterinario(autor);
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new IllegalArgumentException("El diagnóstico no puede estar vacío.");
        }
        this.diagnostico = diagnostico;
    }

    /** Sin peso registrado no se indica ningún medicamento. */
    public void indicarTratamiento(Persona autor, Tratamiento tratamiento)
            throws PesoNoRegistradoException {
        exigirAbierta();
        exigirVeterinario(autor);
        if (tratamiento == null) {
            throw new IllegalArgumentException("El tratamiento es obligatorio.");
        }
        if (pesoDelDia == null) {
            throw new PesoNoRegistradoException(
                "Registrá el peso del animal antes de indicar un medicamento.");
        }
        tratamientos.add(tratamiento);
    }

    /**
     * Aplica una vacuna con el número de lote indicado.
     * EdadMinimaVacunaException si la mascota es muy chica; IllegalArgumentException
     * si la vacuna no corresponde a su especie.
     */
    public void aplicarVacuna(Vacuna vacuna, String lote)
            throws EdadMinimaVacunaException {
        exigirAbierta();
        if (vacuna == null) {
            throw new IllegalArgumentException("La vacuna es obligatoria.");
        }
        if (lote == null || lote.isBlank()) {
            throw new IllegalArgumentException("El lote de la vacuna es obligatorio.");
        }
        vacuna.validarAplicacion(turno.getMascota());
        vacunas.add(new VacunaAplicada(fecha, lote, vacuna));
    }

    /**
     * Confirma la consulta: desde ahora no se modifica más. Si hay un error se corrige
     * con una nota rectificatoria (HistoriaClinica.rectificar).
     */
    public void cerrar() {
        cerrada = true;
    }

    public boolean estaCerrada() {
        return cerrada;
    }

    private void exigirAbierta() {
        if (cerrada) {
            throw new IllegalStateException("La consulta ya fue confirmada y no se puede modificar: "
                    + "corríjala con una nota rectificatoria.");
        }
    }

    private void exigirVeterinario(Persona autor) {
        if (!(autor instanceof Veterinario)) {
            throw new IllegalArgumentException(
                "Solo un veterinario puede registrar diagnósticos y tratamientos.");
        }
    }

    public void setObservaciones(String observaciones) {
        exigirAbierta();
        this.observaciones = observaciones;
    }

    public Turno getTurno() {
        return turno;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Double getPesoDelDia() {
        return pesoDelDia;
    }

    public List<Tratamiento> getTratamientos() {
        return Collections.unmodifiableList(tratamientos);
    }

    public List<VacunaAplicada> getVacunas() {
        return Collections.unmodifiableList(vacunas);
    }
}
