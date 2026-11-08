import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import modelo.*;
import modelo.excepciones.*;

/** Pruebas de la atención clínica: consulta, vacunas y nota rectificatoria. */
public class PruebasClinica {

    private static final LocalDateTime LUNES_10 = LocalDateTime.of(2026, 11, 9, 10, 0);

    public static void main(String[] args) {
        ejecutar();
        Prueba.terminar();
    }

    public static void ejecutar() {
        Prueba.titulo("Atención clínica");
        Prueba.correr("no se indica un medicamento sin peso registrado", PruebasClinica::medicamentoSinPeso);
        Prueba.correr("la vacuna exige una edad mínima", PruebasClinica::vacunaConEdadMinima);
        Prueba.correr("la nota rectificatoria no modifica la consulta", PruebasClinica::notaRectificatoria);
        Prueba.correr("solo un veterinario firma la nota", PruebasClinica::soloVeterinarioFirma);
        Prueba.correr("una consulta cerrada no se puede modificar", PruebasClinica::consultaCerrada);
    }

    private static Veterinario veterinario() {
        Veterinario v = new Veterinario("Laura", "Gómez", "30111222", "11-5555", "MP123", "Clínica general");
        v.agregarHorario(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        return v;
    }

    /** Agenda un turno y registra la consulta de esa mascota. */
    private static Consulta consultaDe(Mascota mascota, Veterinario vet, TipoAtencion tipo) throws Exception {
        Turno turno = new Turno(mascota, vet, LUNES_10, tipo);
        vet.agendarTurno(turno);
        return mascota.getHistoriaClinica().registrarConsulta(turno, "Control");
    }

    private static void medicamentoSinPeso() throws Exception {
        Veterinario vet = veterinario();
        Perro rex = new Perro("Rex", 12.5, "Mestizo", "M", LocalDate.now().minusYears(2), "CHIP1", "mediano");
        Consulta consulta = consultaDe(rex, vet, TipoAtencion.CONSULTA_GENERAL);
        Tratamiento tratamiento = new Tratamiento(new Producto("A1", "Amoxicilina"), 5, "cada 12 horas", 7);

        Prueba.esperar(PesoNoRegistradoException.class, () -> consulta.indicarTratamiento(vet, tratamiento),
                "indicar un tratamiento sin peso");

        consulta.registrarPeso(13.2);
        consulta.indicarTratamiento(vet, tratamiento);
        Prueba.afirmar(consulta.getTratamientos().size() == 1, "con el peso registrado el tratamiento tendría que entrar");
        Prueba.afirmar(rex.getPesoActual() == 13.2, "el peso de la consulta tendría que actualizar el de la mascota");
    }

    private static void vacunaConEdadMinima() throws Exception {
        Veterinario vet = veterinario();
        Perro cachorro = new Perro("Toto", 2, "Mestizo", "M", LocalDate.now().minusMonths(2), "CHIP3", "chico");
        Consulta consulta = consultaDe(cachorro, vet, TipoAtencion.VACUNACION);
        Vacuna rabia = new Vacuna("Antirrábica", 4, 365, List.of(Especie.PERRO, Especie.GATO));

        Prueba.esperar(EdadMinimaVacunaException.class, () -> consulta.aplicarVacuna(rabia, "L-101"),
                "cachorro de 2 meses con una vacuna que pide 4");
        Prueba.afirmar(consulta.getVacunas().isEmpty(), "la vacuna rechazada no tendría que quedar registrada");
    }

    private static void notaRectificatoria() throws Exception {
        Veterinario vet = veterinario();
        Perro rex = new Perro("Rex", 12.5, "Mestizo", "M", LocalDate.now().minusYears(2), "CHIP1", "mediano");
        Consulta consulta = consultaDe(rex, vet, TipoAtencion.CONSULTA_GENERAL);
        consulta.registrarDiagnostico(vet, "Otitis");
        HistoriaClinica historia = rex.getHistoriaClinica();

        NotaRectificatoria nota = historia.rectificar(consulta, vet, "El diagnóstico correcto es dermatitis.",
                LUNES_10.toLocalDate());

        Prueba.afirmar(historia.getNotasDe(consulta).contains(nota), "la nota tendría que estar en la historia");
        Prueba.afirmar("Otitis".equals(consulta.getDiagnostico()), "la consulta original no se tendría que modificar");
        Prueba.afirmar(nota.getFirma().contains("Gómez") && nota.getFirma().contains("MP123"),
                "la firma tendría que incluir el apellido y la matrícula");
    }

    private static void soloVeterinarioFirma() throws Exception {
        Veterinario vet = veterinario();
        Recepcionista recepcionista = new Recepcionista("Rita", "Sosa", "27999888", "11-3333");
        Perro rex = new Perro("Rex", 12.5, "Mestizo", "M", LocalDate.now().minusYears(2), "CHIP1", "mediano");
        Consulta consulta = consultaDe(rex, vet, TipoAtencion.CONSULTA_GENERAL);
        HistoriaClinica historia = rex.getHistoriaClinica();

        Prueba.esperar(IllegalArgumentException.class,
                () -> historia.rectificar(consulta, recepcionista, "Texto", LUNES_10.toLocalDate()),
                "nota firmada por la recepcionista");
        Prueba.esperar(IllegalArgumentException.class,
                () -> historia.rectificar(consulta, vet, "  ", LUNES_10.toLocalDate()),
                "nota sin texto");
        Prueba.afirmar(historia.getNotas().isEmpty(), "las notas rechazadas no tendrían que guardarse");
    }

    private static void consultaCerrada() throws Exception {
        Veterinario vet = veterinario();
        Perro rex = new Perro("Rex", 12.5, "Mestizo", "M", LocalDate.now().minusYears(2), "CHIP1", "mediano");
        Consulta consulta = consultaDe(rex, vet, TipoAtencion.CONSULTA_GENERAL);
        consulta.registrarPeso(12);

        consulta.cerrar();

        Prueba.esperar(IllegalStateException.class, () -> consulta.registrarPeso(13), "cambiar el peso");
        Prueba.esperar(IllegalStateException.class, () -> consulta.registrarDiagnostico(vet, "Sano"),
                "cambiar el diagnóstico");
    }
}
