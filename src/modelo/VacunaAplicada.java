package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class VacunaAplicada implements Serializable {
    private static final int DIAS_RECORDATORIO = 7;
    private static final int DIAS_VENCIMIENTO = 30;

    private LocalDate fecha;
    private String lote;
    private Vacuna vacuna;

    public VacunaAplicada(LocalDate fecha, String lote, Vacuna vacuna) {
        this.fecha = fecha;
        this.lote = lote;
        this.vacuna = vacuna;
    }

    public LocalDate getProximoRefuerzo() {
        return fecha.plusDays(vacuna.getIntervaloRefuerzoDias());
    }

    
    public boolean necesitaRecordatorio(LocalDate hoy) {
        LocalDate refuerzo = getProximoRefuerzo();
        return !hoy.isBefore(refuerzo.minusDays(DIAS_RECORDATORIO))
                && !hoy.isAfter(refuerzo);
    }

    
    public boolean estaVencida(LocalDate hoy) {
        return hoy.isAfter(getProximoRefuerzo().plusDays(DIAS_VENCIMIENTO));
    }

    public LocalDate getFecha() { 
        return fecha; }

    public String getLote() { 
        return lote; }

    public Vacuna getVacuna() { 
        return vacuna; }
}