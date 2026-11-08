package modelo;

import java.time.LocalDate;

public class Ave extends Mascota {
    private static final long serialVersionUID = 1L;

    private boolean puedeVolar;

    public Ave(String nombre, double pesoActual, String raza, String sexo,LocalDate fechaNacimiento, boolean puedeVolar) {
        super(nombre, pesoActual, raza, sexo, fechaNacimiento);
        this.puedeVolar = puedeVolar;
    }

    @Override
    public String getCalendarioVacunacion() {
        return "Ave: vacuna polivalente a las 6 semanas con refuerzo anual.";
    }
    
    @Override
    public Especie getEspecie() { return Especie.AVE; }

    public boolean isPuedeVolar() {
        return puedeVolar;
    }
}
