package modelo;

import java.time.LocalDate;

public class Conejo extends Mascota {
    public Conejo(String nombre, double pesoActual, String raza, String sexo,LocalDate fechaNacimiento) {
        super(nombre, pesoActual, raza, sexo, fechaNacimiento);
    }
}

@Override
public Especie getEspecie() { return Especie.CONEJO; }
