package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import modelo.excepciones.SeniaPendienteException;

public class Duenio extends Persona {
    private static final long serialVersionUID = 1L;

    private String email;
    private PlanSalud planSalud;          // opcional: puede ser null
    private List<Mascota> mascotas;
    private List<Notificacion> notificaciones = new ArrayList<>();
    private List<Senia> senias = new ArrayList<>();
    // Fecha del último turno ausente que ya está cubierto por una seña pagada (null si no hay).
    private LocalDateTime ultimaAusenciaCubierta;

    /** Con esta cantidad de ausencias seguidas hay que pagar una seña para pedir turnos. */
    public static final int AUSENCIAS_PARA_SENIA = 3;

    public Duenio(String nombre, String apellido, String DNI, String telefono,
                  String email, PlanSalud planSalud) {
        super(nombre, apellido, DNI, telefono);
        this.email = email;
        this.planSalud = planSalud;
        this.mascotas = new ArrayList<>();
    }

    @Override
    public String descripcion() {
        String plan = (planSalud == null) ? "Sin plan" : planSalud.toString();
        return "Dueño: \n" + super.descripcion() + "\nEmail: " + email
                + "\nPlan de Salud: " + plan;
    }

    /**
     * Única forma correcta de asociar una mascota a un dueño: mantiene
     * sincronizadas las dos puntas de la relación (lista del dueño y
     * referencia de la mascota). Si la mascota tenía otro dueño, la saca de su lista.
     */
    public void agregarMascota(Mascota mascota) {
        if (mascota == null) {
            throw new IllegalArgumentException("La mascota es obligatoria.");
        }
        if (mascotas.contains(mascota)) {
            return;
        }
        Duenio anterior = mascota.getDuenio();
        if (anterior != null) {
            anterior.mascotas.remove(mascota);
        }
        mascotas.add(mascota);
        mascota.setDuenio(this);
    }

    public void quitarMascota(Mascota mascota) {
        if (mascotas.remove(mascota)) {
            mascota.setDuenio(null);
        }
    }

    // ---- Ausencias y seña ----------------------------------------------------

    /**
     * Ausencias seguidas: cuántos de los últimos turnos ya resueltos (atendidos o ausentes) de
     * sus mascotas terminaron en ausencia, sin ningún turno atendido en el medio. Los turnos
     * cancelados con aviso no cuentan ni cortan la racha. Patrón Experto: el dueño conoce a sus mascotas.
     */
    public int getAusenciasSeguidas() {
        return ausenciasRecientes().size();
    }

    /** Turnos ausentes de la racha actual, del más reciente al más antiguo. */
    private List<Turno> ausenciasRecientes() {
        List<Turno> resueltos = new ArrayList<>();
        for (Mascota m : mascotas) {
            for (Turno t : m.getTurnos()) {
                if (t.getEstado() == Estado.ATENDIDO || t.getEstado() == Estado.AUSENTE) {
                    resueltos.add(t);
                }
            }
        }
        resueltos.sort(Comparator.comparing(Turno::getFechaHora).reversed());
        List<Turno> racha = new ArrayList<>();
        for (Turno t : resueltos) {
            if (t.getEstado() != Estado.AUSENTE) {
                break;
            }
            racha.add(t);
        }
        return racha;
    }

    /** ¿Tiene tres o más ausencias seguidas y todavía no pagó la seña correspondiente? */
    public boolean requiereSenia() {
        List<Turno> racha = ausenciasRecientes();
        if (racha.size() < AUSENCIAS_PARA_SENIA) {
            return false;
        }
        LocalDateTime ultima = racha.get(0).getFechaHora();
        return ultimaAusenciaCubierta == null || ultima.isAfter(ultimaAusenciaCubierta);
    }

    /** Se llama antes de asignar un turno: lanza la excepción si falta pagar la seña. */
    public void verificarPuedePedirTurno() throws SeniaPendienteException {
        if (requiereSenia()) {
            throw new SeniaPendienteException(getNombre() + " " + getApellido() + " tiene "
                    + getAusenciasSeguidas() + " ausencias seguidas: debe pagar una seña para pedir nuevos turnos.");
        }
    }

    /** Registra el pago de la seña. Patrón Creador: el dueño contiene sus señas. */
    public Senia registrarSenia(double monto, LocalDate fecha) {
        if (!requiereSenia()) {
            throw new IllegalStateException("El dueño no tiene una seña pendiente.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de la seña debe ser mayor a cero.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la seña es obligatoria.");
        }
        Senia senia = new Senia(monto, fecha);
        senias.add(senia);
        ultimaAusenciaCubierta = ausenciasRecientes().get(0).getFechaHora();
        return senia;
    }

    public List<Senia> getSenias() {
        return Collections.unmodifiableList(senias);
    }

    // ---- Notificaciones ------------------------------------------------------

    public Notificacion notificar(LocalDateTime fecha, String mensaje) {
        if (fecha == null || mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("La fecha y el mensaje de la notificación son obligatorios.");
        }
        Notificacion notificacion = new Notificacion(fecha, mensaje);
        notificaciones.add(notificacion);
        return notificacion;
    }

    public List<Notificacion> getNotificaciones() {
        return Collections.unmodifiableList(notificaciones);
    }

    public String getEmail() {
        return email;
    }

    public PlanSalud getPlanSalud() {
        return planSalud;
    }

    public List<Mascota> getMascotas() {
        return Collections.unmodifiableList(mascotas);
    }
}
