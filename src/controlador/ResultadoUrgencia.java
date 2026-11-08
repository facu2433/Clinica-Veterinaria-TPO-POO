package controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Reprogramacion;
import modelo.Turno;

/** Resultado de atender una urgencia: el turno de la urgencia y los turnos que desplazó. */
public class ResultadoUrgencia {
    private final Turno turno;
    private final List<Reprogramacion> desplazados;

    public ResultadoUrgencia(Turno turno, List<Reprogramacion> desplazados) {
        this.turno = turno;
        this.desplazados = new ArrayList<>(desplazados);
    }

    public Turno getTurno() {
        return turno;
    }

    public List<Reprogramacion> getDesplazados() {
        return Collections.unmodifiableList(desplazados);
    }
}
