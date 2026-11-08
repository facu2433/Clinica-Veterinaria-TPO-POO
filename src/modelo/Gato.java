package modelo;

import java.time.LocalDate;

public class Gato extends Mascota {
    private static final long serialVersionUID = 1L;

    private Entorno entorno;

    public Gato(String nombre, double pesoActual, String raza, String sexo,LocalDate fechaNacimiento, Entorno entorno) {
        super(nombre, pesoActual, raza, sexo, fechaNacimiento);
        this.entorno = entorno;
    }

    @Override
    public String getCalendarioVacunacion() {
        return "Gato: triple felina a las 8 semanas con refuerzo a las 12, "+ "antirrábica a los 4 meses y refuerzo anual.";
}
    @Override
    public Especie getEspecie() { return Especie.GATO; }    

    public Entorno getEntorno() {
        return entorno;
    }

}
