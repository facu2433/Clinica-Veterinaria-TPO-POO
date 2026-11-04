package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoriaClinica implements Serializable {
    private List<Consulta> consultas = new ArrayList<>();

    public Consulta registrarConsulta(Turno turno, String motivo) {
        Consulta consulta = new Consulta(turno, motivo);
        consultas.add(consulta);
        return consulta;
    }

    public List<Consulta> getConsultas() {
        return Collections.unmodifiableList(consultas);
    }
}