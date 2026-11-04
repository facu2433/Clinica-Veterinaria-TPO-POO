package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import modelo.excepciones.EdadMinimaVacunaException;

public class Vacuna implements Serializable {
    private String nombre;
    private int edadMinimaMeses;
    private int intervaloRefuerzoDias;
    private List<Especie> especies;

    public Vacuna(String nombre, int edadMinimaMeses, int intervaloRefuerzoDias,
                  List<Especie> especies) {
        if (edadMinimaMeses < 0 || intervaloRefuerzoDias <= 0) {
            throw new IllegalArgumentException("Edad mínima o intervalo inválidos.");
        }
        this.nombre = nombre;
        this.edadMinimaMeses = edadMinimaMeses;
        this.intervaloRefuerzoDias = intervaloRefuerzoDias;
        this.especies = new ArrayList<>(especies);
    }

    public boolean seAplicaA(Especie especie) {
        return especies.contains(especie);
    }

    
    public void validarAplicacion(Mascota mascota) throws EdadMinimaVacunaException {
        if (!seAplicaA(mascota.getEspecie())) {
            throw new IllegalArgumentException(
                "La vacuna " + nombre + " no se aplica a " + mascota.getEspecie() + ".");
        }
        if (mascota.getEdadEnMeses() < edadMinimaMeses) {
            throw new EdadMinimaVacunaException(
                mascota.getNombre() + " tiene " + mascota.getEdadEnMeses()
                + " meses y la vacuna " + nombre + " exige al menos "
                + edadMinimaMeses + ".");
        }
    }

    public String getNombre() { 
        return nombre; }

    public int getEdadMinimaMeses() { 
        return edadMinimaMeses; }

    public int getIntervaloRefuerzoDias() {
         return intervaloRefuerzoDias; }

    public List<Especie> getEspecies() { 
        return new ArrayList<>(especies); }
}