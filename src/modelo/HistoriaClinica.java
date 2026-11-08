package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** La historia clínica solo crece: no hay métodos para borrar ni modificar consultas. */
public class HistoriaClinica implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Consulta> consultas = new ArrayList<>();
    private List<NotaRectificatoria> notas = new ArrayList<>();

    /**
     * Patrón Creador: la historia contiene las consultas, por eso las crea.
     * La consulta pasa el turno a ATENDIDO; si el turno ya no está programado
     * lanza IllegalStateException y la consulta no se agrega.
     */
    public Consulta registrarConsulta(Turno turno, String motivo) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno es obligatorio.");
        }
        if (turno.getMascota().getHistoriaClinica() != this) {
            throw new IllegalArgumentException(
                "El turno pertenece a otra mascota: no se puede cargar en esta historia clínica.");
        }
        Consulta consulta = new Consulta(turno, motivo);
        consultas.add(consulta);
        return consulta;
    }

    /**
     * Un error de una consulta no se edita: se corrige con una nota rectificatoria firmada
     * por un veterinario. La consulta original queda igual y la nota se agrega a la historia.
     * Patrón Creador: la historia contiene las notas, por eso las crea.
     */
    public NotaRectificatoria rectificar(Consulta consulta, Persona autor, String texto, LocalDate fecha) {
        if (consulta == null || !consultas.contains(consulta)) {
            throw new IllegalArgumentException("La consulta no pertenece a esta historia clínica.");
        }
        if (!(autor instanceof Veterinario)) {
            throw new IllegalArgumentException("Solo un veterinario puede firmar una nota rectificatoria.");
        }
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de la nota rectificatoria es obligatorio.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la nota rectificatoria es obligatoria.");
        }
        NotaRectificatoria nota = new NotaRectificatoria(consulta, (Veterinario) autor, texto, fecha);
        notas.add(nota);
        return nota;
    }

    public List<NotaRectificatoria> getNotas() {
        return Collections.unmodifiableList(notas);
    }

    /** Notas que corrigen una consulta determinada. */
    public List<NotaRectificatoria> getNotasDe(Consulta consulta) {
        List<NotaRectificatoria> resultado = new ArrayList<>();
        for (NotaRectificatoria n : notas) {
            if (n.getConsulta() == consulta) {
                resultado.add(n);
            }
        }
        return Collections.unmodifiableList(resultado);
    }

    public List<Consulta> getConsultas() {
        return Collections.unmodifiableList(consultas);
    }
}
