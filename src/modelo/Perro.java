package modelo;

import java.time.LocalDate;

public class Perro extends Mascota {
    private String numeroChip;
    private String tamanio;

    public Perro(String nombre, double pesoActual, String raza, String sexo,LocalDate fechaNacimiento, String numeroChip, String tamanio) {
        super(nombre, pesoActual, raza, sexo, fechaNacimiento);
        this.numeroChip = numeroChip;
        this.tamanio = tamanio;
    }

    @Override
    public String getCalendarioVacunacion() {
        return "Perro: quíntuple a las 8 semanas con refuerzos cada 4 semanas, "+ "antirrábica a los 4 meses y refuerzo anual.";
    }

    public String getNumeroChip() {
        return numeroChip;
    }

    public String getTamanio() {
        return tamanio;
    }

    
}
