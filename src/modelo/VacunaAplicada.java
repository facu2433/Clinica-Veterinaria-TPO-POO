package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class VacunaAplicada implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int DIAS_RECORDATORIO = 7;    // se avisa desde 7 días antes del refuerzo
    private static final int DIAS_VENCIMIENTO = 30;    // vencida: más de 30 días después del refuerzo

    private LocalDate fecha;
    private String lote;
    private Vacuna vacuna;

    public VacunaAplicada(LocalDate fecha, String lote, Vacuna vacuna) {
        if (fecha == null || vacuna == null || lote == null || lote.isBlank()) {
            throw new IllegalArgumentException("Fecha, lote y vacuna son obligatorios.");
        }
        this.fecha = fecha;
        this.lote = lote;
        this.vacuna = vacuna;
    }

    public LocalDate getProximoRefuerzo() {
        return fecha.plusDays(vacuna.getIntervaloRefuerzoDias());
    }

    /**
     * Hay que recordar el refuerzo desde 7 días antes de la fecha de refuerzo
     * y mientras la vacuna todavía no esté vencida.
     */
    public boolean necesitaRecordatorio(LocalDate hoy) {
        return !hoy.isBefore(getProximoRefuerzo().minusDays(DIAS_RECORDATORIO))
                && !estaVencida(hoy);
    }

    /** Vencida: pasaron más de 30 días desde la fecha de refuerzo sin aplicarlo. */
    public boolean estaVencida(LocalDate hoy) {
        return hoy.isAfter(getProximoRefuerzo().plusDays(DIAS_VENCIMIENTO));
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getLote() {
        return lote;
    }

    public Vacuna getVacuna() {
        return vacuna;
    }
}
