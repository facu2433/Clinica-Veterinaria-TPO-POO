package modelo;

import java.time.LocalDate;

public class Conejo extends Mascota {
    private static final long serialVersionUID = 1L;


    public Conejo(String nombre, double pesoActual, String raza, String sexo,
                  LocalDate fechaNacimiento) {
        super(nombre, pesoActual, raza, sexo, fechaNacimiento);
    }

    @Override
    public String getCalendarioVacunacion() {
        return "Conejo: mixomatosis y enfermedad hemorrágica viral desde las 6 semanas, "
             + "con refuerzo cada 6 a 12 meses.";
    }

    @Override
    public Especie getEspecie() {
        return Especie.CONEJO;
    }
}