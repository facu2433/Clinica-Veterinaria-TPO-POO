package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Mascota implements Serializable {
    protected String nombre;
    protected double pesoActual;
    protected String raza;
    protected String sexo;
    protected LocalDate fechaNacimiento;

    private Duenio duenio;

    public Mascota(String nombre, double pesoActual, String raza,String sexo, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.raza = raza;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        if (pesoActual <= 0) {
            throw new IllegalArgumentException("El peso actual debe ser mayor a 0.");
        }
        this.pesoActual = pesoActual;
    }

    public abstract String getCalendarioVacunacion();

    public int getEdadEnMeses() {
        return (int) ChronoUnit.MONTHS.between(fechaNacimiento, LocalDate.now());
    }


    public void setDuenio(Duenio duenio) {
        this.duenio = duenio;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPesoActual() {
        return pesoActual;
    }

    public String getRaza() {
        return raza;
    }

    public String getSexo() {
        return sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Duenio getDuenio() {
        return duenio;
    }
}
