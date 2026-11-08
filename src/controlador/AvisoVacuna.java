package controlador;

import modelo.Mascota;
import modelo.VacunaAplicada;

/** Resultado del reporte de vacunas: qué mascota necesita qué refuerzo y si ya venció. */
public class AvisoVacuna {
    private final Mascota mascota;
    private final VacunaAplicada aplicacion;
    private final boolean vencida;

    public AvisoVacuna(Mascota mascota, VacunaAplicada aplicacion, boolean vencida) {
        this.mascota = mascota;
        this.aplicacion = aplicacion;
        this.vencida = vencida;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public VacunaAplicada getAplicacion() {
        return aplicacion;
    }

    /** true: pasaron más de 30 días del refuerzo; false: está dentro de la ventana de recordatorio. */
    public boolean isVencida() {
        return vencida;
    }
}
