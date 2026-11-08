import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import controlador.ControladorClinica;
import controlador.DatosDeEjemplo;
import controlador.ResultadoUrgencia;
import modelo.*;
import modelo.excepciones.*;
import persistencia.RepositorioArchivo;
import persistencia.RepositorioMemoria;

/** Pruebas del controlador: casos de uso completos con los datos de ejemplo y persistencia. */
public class PruebasControlador {

    private static final LocalDateTime LUNES_10 = LocalDateTime.of(2026, 11, 9, 10, 0);

    public static void main(String[] args) {
        ejecutar();
        Prueba.terminar();
    }

    public static void ejecutar() {
        Prueba.titulo("Controlador y persistencia");
        Prueba.correr("facturar descuenta el stock y cierra la consulta", PruebasControlador::facturarDescuentaStock);
        Prueba.correr("facturar sin stock no deja nada a medias", PruebasControlador::facturarSinStock);
        Prueba.correr("una urgencia avisa al dueño del turno desplazado", PruebasControlador::urgenciaAvisaAlDuenio);
        Prueba.correr("los datos se conservan al reiniciar", PruebasControlador::datosSobrevivenAlReiniciar);
    }

    private static ControladorClinica controladorConEjemplo() throws Exception {
        ControladorClinica c = new ControladorClinica(new RepositorioMemoria(), new CalculadorDeFactura());
        DatosDeEjemplo.cargar(c);
        return c;
    }

    private static Mascota mascota(ControladorClinica c, String nombre) {
        for (Mascota m : c.getMascotas()) {
            if (m.getNombre().equals(nombre)) {
                return m;
            }
        }
        throw new IllegalStateException("No existe la mascota " + nombre);
    }

    private static Veterinario veterinario(ControladorClinica c, String apellido) {
        for (Veterinario v : c.getVeterinarios()) {
            if (v.getApellido().equals(apellido)) {
                return v;
            }
        }
        throw new IllegalStateException("No existe el veterinario " + apellido);
    }

    private static Producto producto(ControladorClinica c, String codigo) {
        for (Producto p : c.getProductos()) {
            if (p.getCodigo().equals(codigo)) {
                return p;
            }
        }
        throw new IllegalStateException("No existe el producto " + codigo);
    }

    private static void facturarDescuentaStock() throws Exception {
        ControladorClinica c = controladorConEjemplo();
        Mascota rex = mascota(c, "Rex");                  // la dueña tiene plan de salud del 15 %
        Producto amoxicilina = producto(c, "MED-001");
        int stockAntes = amoxicilina.getStockActual();

        Turno turno = c.asignarTurno(rex, veterinario(c, "Gómez"), LUNES_10, TipoAtencion.CONSULTA_GENERAL);
        Consulta consulta = c.registrarConsulta(turno, "Control anual");
        Map<Producto, Integer> insumos = new LinkedHashMap<>();
        insumos.put(amoxicilina, 3);

        Factura factura = c.facturar(consulta, insumos, new PagoEfectivo());

        Prueba.afirmar(factura.getNumero() == 1 && c.getFacturas().contains(factura), "tendría que haber una factura N° 1");
        // consulta 20000 + 3 x 1500 = 24500; el plan (15 %) le gana al efectivo (10 %)
        Prueba.afirmarImporte(20825, c.getTotal(factura), "total de la factura");
        Prueba.afirmar(amoxicilina.getStockActual() == stockAntes - 3, "tendrían que haberse descontado 3 unidades");
        Prueba.afirmar(consulta.estaCerrada(), "la consulta facturada tendría que quedar cerrada");
        Prueba.afirmar(!c.getConsultasSinFacturar().contains(consulta), "ya no tendría que figurar como pendiente");
    }

    private static void facturarSinStock() throws Exception {
        ControladorClinica c = controladorConEjemplo();
        Mascota rex = mascota(c, "Rex");
        Producto amoxicilina = producto(c, "MED-001");
        Producto suero = producto(c, "INS-001");                // quedan 6 unidades
        int amoxAntes = amoxicilina.getStockActual();

        Turno turno = c.asignarTurno(rex, veterinario(c, "Gómez"), LUNES_10, TipoAtencion.CONSULTA_GENERAL);
        Consulta consulta = c.registrarConsulta(turno, "Control");
        Map<Producto, Integer> pedido = new LinkedHashMap<>();
        pedido.put(amoxicilina, 1);
        pedido.put(suero, 7);

        Prueba.esperar(StockInsuficienteException.class, () -> c.facturar(consulta, pedido, new PagoEfectivo()),
                "pedir 7 de suero cuando hay 6");

        Prueba.afirmar(amoxicilina.getStockActual() == amoxAntes, "no tendría que haberse descontado la amoxicilina");
        Prueba.afirmar(c.getFacturas().isEmpty(), "no tendría que haberse emitido ninguna factura");
        Prueba.afirmar(!consulta.estaCerrada(), "la consulta tendría que seguir abierta");
    }

    private static void urgenciaAvisaAlDuenio() throws Exception {
        ControladorClinica c = controladorConEjemplo();
        Veterinario gomez = veterinario(c, "Gómez");               // de guardia en los datos de ejemplo
        LocalDateTime nueve = LocalDateTime.of(2026, 11, 9, 9, 0);
        Mascota rex = mascota(c, "Rex");
        Turno comun = c.asignarTurno(rex, gomez, nueve, TipoAtencion.CONSULTA_GENERAL);

        ResultadoUrgencia r = c.asignarUrgencia(mascota(c, "Bugs"), TipoAtencion.CONSULTA_GENERAL,
                LocalDateTime.of(2026, 11, 9, 8, 0));

        Prueba.afirmar(r.getTurno().getVeterinario() == gomez && r.getTurno().getFechaHora().equals(nueve),
                "la urgencia tendría que ir con el veterinario de guardia en el primer horario");
        Prueba.afirmar(comun.getFechaHora().equals(nueve.plusMinutes(30)), "el turno común tendría que pasar a las 9:30");
        Duenio ana = rex.getDuenio();
        Prueba.afirmar(ana.getNotificaciones().size() == 1
                        && ana.getNotificaciones().get(0).getMensaje().contains("Rex"),
                "la dueña de Rex tendría que recibir un aviso");
    }

    private static void datosSobrevivenAlReiniciar() throws Exception {
        File archivo = File.createTempFile("clinica-prueba", ".dat");
        archivo.delete();                      // sin archivo, como en la primera ejecución
        archivo.deleteOnExit();

        ControladorClinica c = new ControladorClinica(new RepositorioArchivo(archivo.getPath()), new CalculadorDeFactura());
        DatosDeEjemplo.cargar(c);
        c.asignarTurno(mascota(c, "Rex"), veterinario(c, "Gómez"), LUNES_10, TipoAtencion.CONSULTA_GENERAL);

        // un controlador nuevo lee el mismo archivo, como al volver a abrir el programa
        ControladorClinica reiniciado = new ControladorClinica(new RepositorioArchivo(archivo.getPath()),
                new CalculadorDeFactura());

        Prueba.afirmar(reiniciado.getMascotas().size() == 4, "tendrían que estar las 4 mascotas");
        Prueba.afirmar(reiniciado.getTurnosProgramados().size() == 1, "tendría que estar el turno asignado");
        Prueba.afirmar(mascota(reiniciado, "Rex").getDuenio() != null, "Rex tendría que seguir con su dueño");
    }
}
