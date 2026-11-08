package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Aviso que la clínica le deja a un dueño (por ejemplo, un turno reprogramado). */
public class Notificacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDateTime fecha;
    private final String mensaje;

    Notificacion(LocalDateTime fecha, String mensaje) {
        this.fecha = fecha;
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getMensaje() {
        return mensaje;
    }
}
