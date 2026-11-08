package modelo;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Corrección firmada por un veterinario sobre una consulta de la historia clínica.
 * La consulta original no se toca: la nota se agrega a la historia y la acompaña.
 */
public class NotaRectificatoria implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Consulta consulta;
    private final Veterinario autor;
    private final String texto;
    private final LocalDate fecha;

    // Package-private: solo la crea HistoriaClinica.rectificar (patrón Creador).
    NotaRectificatoria(Consulta consulta, Veterinario autor, String texto, LocalDate fecha) {
        this.consulta = consulta;
        this.autor = autor;
        this.texto = texto;
        this.fecha = fecha;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public Veterinario getAutor() {
        return autor;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    /** Firma del profesional: apellido, nombre y matrícula. */
    public String getFirma() {
        return autor.getApellido() + ", " + autor.getNombre() + " (Mat. " + autor.getMatricula() + ")";
    }
}
