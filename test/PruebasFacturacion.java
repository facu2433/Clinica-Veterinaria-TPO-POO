import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import modelo.*;
import modelo.excepciones.*;

/** Pruebas de stock y facturación. */
public class PruebasFacturacion {

    public static void main(String[] args) {
        ejecutar();
        Prueba.terminar();
    }

    public static void ejecutar() {
        Prueba.titulo("Stock y facturación");
        Prueba.correr("el stock se descuenta primero del lote que vence antes", PruebasFacturacion::stockUsaElLoteQueVenceAntes);
        Prueba.correr("sin stock suficiente no se descuenta nada", PruebasFacturacion::stockInsuficiente);
        Prueba.correr("avisa cuando el stock llega al mínimo", PruebasFacturacion::alertaDeReposicion);
        Prueba.correr("descuento y recargo según el medio de pago", PruebasFacturacion::mediosDePago);
        Prueba.correr("el plan de salud no se acumula con efectivo", PruebasFacturacion::planNoSeAcumula);
        Prueba.correr("una factura emitida se anula con nota de crédito", PruebasFacturacion::anularFactura);
    }

    private static final LocalDate HOY = LocalDate.now();

    private static int cantidadDelLote(Producto producto, String numero) {
        for (LoteProducto lote : producto.getLotes()) {
            if (lote.getNumeroLote().equals(numero)) {
                return lote.getCantidad();
            }
        }
        throw new IllegalStateException("No existe el lote " + numero);
    }

    /** Arma una factura sin emitir, de una consulta de un dueño con (o sin) plan de salud. */
    private static Factura facturaNueva(PoliticaDeCobro politica, PlanSalud plan) throws Exception {
        Duenio duenio = new Duenio("Ana", "Pérez", "20111222", "11-1111", "ana@mail.com", plan);
        Perro rex = new Perro("Rex", 12, "Mestizo", "M", HOY.minusYears(2), "CHIP1", "mediano");
        duenio.agregarMascota(rex);
        Veterinario vet = new Veterinario("Laura", "Gómez", "30111222", "11-5555", "MP1", "General");
        vet.agregarHorario(new HorarioAtencion(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Turno turno = new Turno(rex, vet, LocalDateTime.of(2026, 11, 9, 10, 0), TipoAtencion.CONSULTA_GENERAL);
        vet.agendarTurno(turno);
        Consulta consulta = rex.getHistoriaClinica().registrarConsulta(turno, "Control");
        return new Factura(consulta, politica);
    }

    private static void stockUsaElLoteQueVenceAntes() throws Exception {
        Producto p = new Producto("A1", "Amoxicilina", 1000, 3);
        p.agregarLote(new LoteProducto("L-VENCIDO", 4, HOY.minusDays(10)));
        p.agregarLote(new LoteProducto("L-LEJANO", 5, HOY.plusDays(30)));
        p.agregarLote(new LoteProducto("L-CERCANO", 5, HOY.plusDays(10)));

        Prueba.afirmar(p.getStockActual() == 10, "el stock solo tendría que sumar los lotes vigentes (10)");
        p.descontarStock(7);

        Prueba.afirmar(cantidadDelLote(p, "L-CERCANO") == 0, "primero tendría que consumirse el lote que vence antes");
        Prueba.afirmar(cantidadDelLote(p, "L-LEJANO") == 3, "el resto tendría que salir del otro lote");
        Prueba.afirmar(cantidadDelLote(p, "L-VENCIDO") == 4, "el lote vencido no se tendría que tocar");
    }

    private static void stockInsuficiente() throws Exception {
        Producto p = new Producto("A1", "Amoxicilina", 1000, 0);
        p.agregarLote(new LoteProducto("L1", 5, HOY.plusDays(10)));
        p.agregarLote(new LoteProducto("L2", 3, HOY.plusDays(20)));

        Prueba.esperar(StockInsuficienteException.class, () -> p.descontarStock(9), "pedir 9 y tener 8");
        Prueba.afirmar(p.getStockActual() == 8, "un pedido rechazado no tendría que descontar nada");

        Producto vencido = new Producto("A2", "Vencido", 1000, 0);
        vencido.agregarLote(new LoteProducto("L-VIEJO", 10, HOY.minusDays(1)));
        Prueba.esperar(LoteVencidoException.class, () -> vencido.descontarStock(1), "usar un lote vencido");
    }

    private static void alertaDeReposicion() throws Exception {
        Producto p = new Producto("A1", "Amoxicilina", 1000, 3);
        p.agregarLote(new LoteProducto("L1", 10, HOY.plusDays(30)));

        Prueba.afirmar(!p.descontarStock(6), "con 4 unidades (mínimo 3) no tendría que avisar");
        Prueba.afirmar(p.descontarStock(1), "con 3 unidades (el mínimo) tendría que avisar");
        Prueba.afirmar(p.necesitaReposicion(), "el producto tendría que necesitar reposición");
    }

    private static void mediosDePago() throws Exception {
        CalculadorDeFactura calc = new CalculadorDeFactura();

        Factura efectivo = facturaNueva(new PagoEfectivo(), null);
        efectivo.agregarLinea("Consulta", 1, 1000, null);
        Prueba.afirmarImporte(900, efectivo.getTotal(calc), "efectivo: 10 % de descuento");

        Factura debito = facturaNueva(new PagoDebito(), null);
        debito.agregarLinea("Consulta", 1, 1000, null);
        Prueba.afirmarImporte(1000, debito.getTotal(calc), "débito: sin cambios");

        Factura credito = facturaNueva(new PagoCreditoCuotas(6), null);
        credito.agregarLinea("Consulta", 1, 1000, null);
        Prueba.afirmarImporte(1080, credito.getTotal(calc), "crédito en cuotas: 8 % de recargo");
    }

    private static void planNoSeAcumula() throws Exception {
        CalculadorDeFactura calc = new CalculadorDeFactura();

        Factura conPlan15 = facturaNueva(new PagoEfectivo(), new PlanSalud("Plus", 15));
        conPlan15.agregarLinea("Consulta", 1, 1000, null);
        Prueba.afirmarImporte(850, conPlan15.getTotal(calc), "efectivo con plan 15 %: se aplica el mayor (15 %)");

        Factura conPlan5 = facturaNueva(new PagoEfectivo(), new PlanSalud("Basico", 5));
        conPlan5.agregarLinea("Consulta", 1, 1000, null);
        Prueba.afirmarImporte(900, conPlan5.getTotal(calc), "efectivo con plan 5 %: se aplica el mayor (10 %)");
    }

    private static void anularFactura() throws Exception {
        CalculadorDeFactura calc = new CalculadorDeFactura();
        Tarifario tarifa = new Tarifario();
        tarifa.definirPrecio(TipoAtencion.CONSULTA_GENERAL, 20000);

        Factura factura = facturaNueva(new PagoDebito(), null);
        factura.agregarLineaConsulta(tarifa);
        factura.emitir(1, calc);

        Prueba.esperar(IllegalStateException.class, () -> factura.agregarLinea("Otra", 1, 100, null),
                "agregar una línea a una factura emitida");

        NotaDeCredito nota = factura.anular("Error de carga");
        Prueba.afirmar(factura.estaAnulada() && factura.getNotaDeCredito() == nota,
                "la factura tendría que quedar anulada con su nota de crédito");
        Prueba.afirmarImporte(20000, nota.getImporte(), "la nota tendría que acreditar el total");
        Prueba.esperar(IllegalStateException.class, () -> factura.anular("otra vez"), "anular dos veces");
    }
}
