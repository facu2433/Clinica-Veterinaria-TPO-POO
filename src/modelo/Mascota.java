package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Mascota implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String nombre;
    protected double pesoActual;
    protected String raza;
    protected String sexo;
    protected LocalDate fechaNacimiento;
    protected HistoriaClinica historiaClinica;

    private Duenio duenio;
    private List<Turno> turnos = new ArrayList<>();   // todos los turnos agendados de la mascota

    public Mascota(String nombre, double pesoActual, String raza, String sexo,
                   LocalDate fechaNacimiento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la mascota es obligatorio.");
        }
        if (pesoActual <= 0) {
            throw new IllegalArgumentException("El peso actual debe ser mayor a 0.");
        }
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "La fecha de nacimiento es obligatoria y no puede ser futura.");
        }
        this.nombre = nombre;
        this.pesoActual = pesoActual;
        this.raza = raza;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        // La historia clínica pertenece a la mascota (no al dueño): sobrevive a un cambio de dueño.
        this.historiaClinica = new HistoriaClinica();
    }

    // Polimorfismo: cada especie define su calendario y su especie, sin condicionales.
    public abstract String getCalendarioVacunacion();

    public abstract Especie getEspecie();

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    /**
     * Transfiere la mascota a otro dueño. La historia clínica se conserva
     * porque es un atributo de la mascota.
     */
    public void cambiarDuenio(Duenio nuevo) {
        if (nuevo == null) {
            throw new IllegalArgumentException("El nuevo dueño es obligatorio.");
        }
        nuevo.agregarMascota(this); // actualiza ambas puntas y saca la mascota del dueño anterior
    }

    public int getEdadEnMeses() {
        return (int) ChronoUnit.MONTHS.between(fechaNacimiento, LocalDate.now());
    }

    // Solo lo usa Duenio (mismo paquete) para mantener sincronizada la relación.
    void setDuenio(Duenio duenio) {
        this.duenio = duenio;
    }

    // Solo lo usa Veterinario (mismo paquete) al agendar: así el dueño puede contar sus ausencias.
    void registrarTurno(Turno turno) {
        turnos.add(turno);
    }

    public List<Turno> getTurnos() {
        return Collections.unmodifiableList(turnos);
    }

    // Solo lo usa Consulta (mismo paquete): el peso cambia al registrarlo en una consulta.
    void actualizarPeso(double nuevoPeso) {
        if (nuevoPeso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero.");
        }
        this.pesoActual = nuevoPeso;
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
