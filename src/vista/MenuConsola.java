package vista;

import controlador.AvisoVacuna;
import controlador.ControladorClinica;
import controlador.DatosDeEjemplo;
import controlador.ResultadoUrgencia;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import modelo.Consulta;
import modelo.Duenio;
import modelo.Entorno;
import modelo.Especie;
import modelo.Factura;
import modelo.LineaFactura;
import modelo.LoteProducto;
import modelo.Mascota;
import modelo.NotaDeCredito;
import modelo.NotaRectificatoria;
import modelo.Notificacion;
import modelo.PagoCreditoCuotas;
import modelo.PagoDebito;
import modelo.PagoEfectivo;
import modelo.PlanSalud;
import modelo.PoliticaDeCobro;
import modelo.Producto;
import modelo.Reprogramacion;
import modelo.Senia;
import modelo.TipoAtencion;
import modelo.Tratamiento;
import modelo.VacunaAplicada;
import modelo.Turno;
import modelo.Vacuna;
import modelo.Veterinario;
import modelo.excepciones.EdadMinimaVacunaException;
import modelo.excepciones.LoteVencidoException;
import modelo.excepciones.PesoNoRegistradoException;
import modelo.excepciones.SeniaPendienteException;
import modelo.excepciones.StockInsuficienteException;
import modelo.excepciones.TurnoSuperpuestoException;

/**
 * Vista de consola. Solo pide datos, llama al controlador y muestra resultados:
 * no contiene reglas de negocio. Las excepciones del modelo se capturan en un único
 * lugar (ejecutar) y se informan con un mensaje claro para el usuario.
 */
public class MenuConsola {

    /** Una acción de menú. Declara todo lo que puede lanzar el controlador. */
    private interface Accion {
        void ejecutar() throws TurnoSuperpuestoException, SeniaPendienteException, PesoNoRegistradoException,
                EdadMinimaVacunaException, StockInsuficienteException, LoteVencidoException, IOException;
    }

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale ES = Locale.forLanguageTag("es-AR");

    private final ControladorClinica controlador;
    private final Entrada entrada;

    public MenuConsola(ControladorClinica controlador) {
        this.controlador = controlador;
        this.entrada = new Entrada(new Scanner(System.in));
    }

    public void iniciar() {
        System.out.println("=== CLÍNICA VETERINARIA PATITAS ===");
        try {
            menuPrincipal();
        } catch (NoSuchElementException e) {
            // Se cerró la entrada de datos (por ejemplo Ctrl+D): salimos ordenadamente
            System.out.println("\nSe cerró la entrada de datos.");
        }
        guardarAlSalir();
        System.out.println("Hasta luego.");
    }

    private void guardarAlSalir() {
        try {
            controlador.guardarTodo();
            System.out.println("Datos guardados.");
        } catch (IOException e) {
            System.out.println("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    // =====================================================================
    // Menús
    // =====================================================================

    private void menuPrincipal() {
        submenu("MENÚ PRINCIPAL", "Guardar y salir",
            new String[] {"Turnos", "Consultas y facturación", "Dueños y mascotas", "Veterinarios",
                          "Stock", "Configuración (precios y vacunas)", "Reportes", "Cargar datos de ejemplo"},
            new Accion[] {this::menuTurnos, this::menuConsultas, this::menuDuenios, this::menuVeterinarios,
                          this::menuStock, this::menuConfiguracion, this::menuReportes, this::cargarEjemplo});
    }

    private void menuTurnos() {
        submenu("TURNOS", "Volver",
            new String[] {"Asignar turno", "Atender una urgencia", "Reprogramar turno", "Cancelar turno",
                          "Marcar ausencia", "Registrar seña de un dueño", "Ver turnos programados"},
            new Accion[] {this::asignarTurno, this::atenderUrgencia, this::reprogramarTurno, this::cancelarTurno,
                          this::marcarAusencia, this::registrarSenia, this::verTurnos});
    }

    private void menuConsultas() {
        submenu("CONSULTAS Y FACTURACIÓN", "Volver",
            new String[] {"Registrar consulta", "Facturar consulta pendiente", "Anular factura", "Ver facturas",
                          "Corregir una consulta (nota rectificatoria)", "Ver historia clínica de una mascota"},
            new Accion[] {this::registrarConsulta, this::facturarPendiente, this::anularFactura, this::verFacturas,
                          this::rectificarConsulta, this::verHistoriaClinica});
    }

    private void menuDuenios() {
        submenu("DUEÑOS Y MASCOTAS", "Volver",
            new String[] {"Registrar dueño", "Registrar mascota", "Cambiar el dueño de una mascota",
                          "Ver dueños y mascotas", "Ver notificaciones de un dueño"},
            new Accion[] {this::registrarDuenio, this::registrarMascota, this::cambiarDuenio, this::verDuenios,
                          this::verNotificaciones});
    }

    private void menuVeterinarios() {
        submenu("VETERINARIOS", "Volver",
            new String[] {"Registrar veterinario", "Cargar horario de atención", "Designar veterinario de guardia",
                          "Ver veterinarios"},
            new Accion[] {this::registrarVeterinario, this::cargarHorario, this::designarGuardia,
                          this::verVeterinarios});
    }

    private void menuStock() {
        submenu("STOCK", "Volver",
            new String[] {"Registrar producto", "Agregar lote a un producto", "Agregar proveedor a un producto",
                          "Ver productos y stock"},
            new Accion[] {this::registrarProducto, this::agregarLote, this::agregarProveedor, this::verProductos});
    }

    private void menuConfiguracion() {
        submenu("CONFIGURACIÓN", "Volver",
            new String[] {"Definir precio de una atención", "Ver precios de atención", "Registrar vacuna en el catálogo"},
            new Accion[] {this::definirPrecio, this::verPrecios, this::registrarVacuna});
    }

    private void menuReportes() {
        submenu("REPORTES", "Volver",
            new String[] {"Turnos por veterinario", "Vacunas próximas a vencer o vencidas",
                          "Productos con stock bajo"},
            new Accion[] {this::reporteTurnosPorVeterinario, this::reporteVacunas, this::reporteStockBajo});
    }

    private void submenu(String titulo, String etiquetaCero, String[] textos, Accion[] acciones) {
        while (true) {
            System.out.println("\n--- " + titulo + " ---");
            for (int i = 0; i < textos.length; i++) {
                System.out.println((i + 1) + ". " + textos[i]);
            }
            System.out.println("0. " + etiquetaCero);
            int opcion = entrada.leerEntero("Seleccione una opción", 0, textos.length);
            if (opcion == 0) {
                return;
            }
            ejecutar(acciones[opcion - 1]);
        }
    }

    /**
     * Único lugar donde se capturan las excepciones del modelo y del controlador.
     * Cada grupo se informa con un mensaje distinto según su origen.
     */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (TurnoSuperpuestoException | SeniaPendienteException | PesoNoRegistradoException
                 | EdadMinimaVacunaException | StockInsuficienteException | LoteVencidoException e) {
            // Situaciones esperables del negocio (excepciones verificadas propias)
            System.out.println("No se pudo completar la operación: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Datos inválidos o acción no permitida en el estado actual
            System.out.println("Operación rechazada: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo de datos: " + e.getMessage());
            System.out.println("La operación quedó en memoria y se volverá a guardar con la próxima operación.");
        }
    }

    // =====================================================================
    // Turnos
    // =====================================================================

    private void asignarTurno() throws TurnoSuperpuestoException, SeniaPendienteException, IOException {
        Mascota mascota = entrada.elegir("Mascotas", controlador.getMascotas(), MenuConsola::describir);
        if (mascota == null) {
            return;
        }
        Veterinario veterinario = entrada.elegir("Veterinarios", controlador.getVeterinarios(),
                MenuConsola::describirConHorarios);
        if (veterinario == null) {
            return;
        }
        TipoAtencion tipo = entrada.elegir("Tipos de atención", Arrays.asList(TipoAtencion.values()),
                t -> t + " (" + t.getDuracionMinutos() + " min)");
        if (tipo == null) {
            return;
        }
        LocalDateTime fechaHora = entrada.leerFechaHora("Fecha y hora del turno");
        Turno turno = controlador.asignarTurno(mascota, veterinario, fechaHora, tipo);
        System.out.println("Turno asignado: " + describir(turno));
    }

    private void atenderUrgencia() throws TurnoSuperpuestoException, IOException {
        Veterinario guardia = controlador.getVeterinarioDeGuardia();
        if (guardia == null) {
            System.out.println("No hay un veterinario de guardia designado (Veterinarios > Designar veterinario de guardia).");
            return;
        }
        System.out.println("Veterinario de guardia: " + guardia.getApellido() + ", " + guardia.getNombre());
        Mascota mascota = entrada.elegir("Mascotas", controlador.getMascotas(), MenuConsola::describir);
        if (mascota == null) {
            return;
        }
        TipoAtencion tipo = entrada.elegir("Tipos de atención", Arrays.asList(TipoAtencion.values()),
                t -> t + " (" + t.getDuracionMinutos() + " min)");
        if (tipo == null) {
            return;
        }
        ResultadoUrgencia resultado = controlador.asignarUrgencia(mascota, tipo, LocalDateTime.now());
        System.out.println("Urgencia asignada: " + describir(resultado.getTurno()));
        if (resultado.getDesplazados().isEmpty()) {
            System.out.println("El horario estaba libre: no se desplazó ningún turno.");
        }
        for (Reprogramacion r : resultado.getDesplazados()) {
            Duenio d = r.getTurno().getMascota().getDuenio();
            System.out.println("Turno desplazado y reprogramado: " + describir(r.getTurno())
                    + " (antes: " + FECHA_HORA.format(r.getFechaAnterior()) + ")");
            System.out.println("   NOTIFICACIÓN para " + d.getNombre() + " " + d.getApellido()
                    + " (" + d.getEmail() + "): " + r.mensajeParaElDuenio());
        }
    }

    private void reprogramarTurno() throws TurnoSuperpuestoException, IOException {
        Turno turno = entrada.elegir("Turnos programados", controlador.getTurnosProgramados(), MenuConsola::describir);
        if (turno == null) {
            return;
        }
        LocalDateTime nueva = entrada.leerFechaHora("Nueva fecha y hora");
        controlador.reprogramarTurno(turno, nueva);
        System.out.println("Turno reprogramado: " + describir(turno));
    }

    private void registrarSenia() throws IOException {
        List<Duenio> deudores = new ArrayList<>();
        for (Duenio d : controlador.getDuenios()) {
            if (d.requiereSenia()) {
                deudores.add(d);
            }
        }
        if (deudores.isEmpty()) {
            System.out.println("Ningún dueño tiene una seña pendiente (se exige con "
                    + Duenio.AUSENCIAS_PARA_SENIA + " ausencias seguidas).");
            return;
        }
        Duenio duenio = entrada.elegir("Dueños con seña pendiente", deudores,
                d -> d.getApellido() + ", " + d.getNombre() + " (" + d.getAusenciasSeguidas() + " ausencias seguidas)");
        if (duenio == null || !entrada.confirmar("¿Registrar el pago de la seña de " + dinero(controlador.getMontoSenia()) + "?")) {
            return;
        }
        Senia senia = controlador.registrarSenia(duenio, LocalDate.now());
        System.out.println("Seña de " + dinero(senia.getMonto()) + " registrada. " + duenio.getNombre()
                + " puede volver a pedir turnos.");
    }

    private void cancelarTurno() throws IOException {
        Turno turno = entrada.elegir("Turnos programados", controlador.getTurnosProgramados(), MenuConsola::describir);
        if (turno == null || !entrada.confirmar("¿Cancelar este turno?")) {
            return;
        }
        double cargo = controlador.cancelarTurno(turno, LocalDateTime.now());
        if (cargo > 0) {
            System.out.println("Turno cancelado con menos de 2 horas de anticipación. Cargo por cancelación: "
                    + dinero(cargo) + " (20 % de la consulta).");
        } else {
            System.out.println("Turno cancelado sin cargo.");
        }
    }

    private void marcarAusencia() throws IOException {
        Turno turno = entrada.elegir("Turnos programados", controlador.getTurnosProgramados(), MenuConsola::describir);
        if (turno == null || !entrada.confirmar("¿Marcar la ausencia del dueño?")) {
            return;
        }
        int seguidas = controlador.marcarAusente(turno);
        System.out.println("Turno marcado como ausente. El horario quedó libre. Ausencias seguidas del dueño: " + seguidas + ".");
        if (turno.getMascota().getDuenio() != null && turno.getMascota().getDuenio().requiereSenia()) {
            System.out.println("ATENCIÓN: con " + Duenio.AUSENCIAS_PARA_SENIA + " ausencias seguidas debe pagar una seña de "
                    + dinero(controlador.getMontoSenia()) + " para pedir nuevos turnos (Turnos > Registrar seña).");
        }
    }

    private void verTurnos() {
        List<Turno> turnos = controlador.getTurnosProgramados();
        if (turnos.isEmpty()) {
            System.out.println("No hay turnos programados.");
        }
        for (Turno t : turnos) {
            System.out.println("- " + describir(t));
        }
    }

    // =====================================================================
    // Consultas y facturación
    // =====================================================================

    private void registrarConsulta() throws IOException {
        Turno turno = entrada.elegir("Turnos a atender", controlador.getTurnosProgramados(), MenuConsola::describir);
        if (turno == null) {
            return;
        }
        String motivo = entrada.leerTexto("Motivo de la consulta");
        final Consulta consulta = controlador.registrarConsulta(turno, motivo);
        System.out.println("Consulta registrada. El turno quedó como ATENDIDO.");

        if (entrada.confirmar("¿Registrar el peso de hoy?")) {
            final double peso = entrada.leerDecimalPositivo("Peso (kg)");
            ejecutar(() -> controlador.registrarPeso(consulta, peso));
        }
        final String diagnostico = entrada.leerTextoOpcional("Diagnóstico (Enter para omitir)");
        if (!diagnostico.isEmpty()) {
            ejecutar(() -> controlador.registrarDiagnostico(consulta, diagnostico));
        }
        while (entrada.confirmar("¿Indicar un tratamiento?")) {
            indicarTratamiento(consulta);
        }
        while (entrada.confirmar("¿Aplicar una vacuna?")) {
            aplicarVacuna(consulta);
        }
        if (entrada.confirmar("¿Confirmar y facturar ahora?")) {
            final Consulta aFacturar = consulta;
            ejecutar(() -> facturarConsulta(aFacturar));
        } else {
            System.out.println("La consulta queda pendiente (Consultas y facturación > Facturar consulta pendiente).");
        }
    }

    private void indicarTratamiento(final Consulta consulta) {
        final Producto medicamento = entrada.elegir("Medicamentos", controlador.getProductos(),
                p -> p.getNombre() + " (stock: " + p.getStockActual() + ")");
        if (medicamento == null) {
            return;
        }
        final double dosis = entrada.leerDecimalPositivo("Dosis");
        final String frecuencia = entrada.leerTexto("Frecuencia (ej. cada 12 horas)");
        final int dias = entrada.leerEntero("Duración en días", 1, 365);
        ejecutar(() -> {
            try {
                controlador.indicarTratamiento(consulta, medicamento, dosis, frecuencia, dias);
                System.out.println("Tratamiento indicado.");
            } catch (PesoNoRegistradoException e) {
                // Excepción propia: se informa y se ofrece resolverlo en el momento
                System.out.println(e.getMessage());
                if (entrada.confirmar("¿Registrar el peso ahora?")) {
                    controlador.registrarPeso(consulta, entrada.leerDecimalPositivo("Peso (kg)"));
                    controlador.indicarTratamiento(consulta, medicamento, dosis, frecuencia, dias);
                    System.out.println("Tratamiento indicado.");
                }
            }
        });
    }

    private void aplicarVacuna(final Consulta consulta) {
        final Vacuna vacuna = entrada.elegir("Vacunas", controlador.getVacunas(),
                v -> v.getNombre() + " (edad mínima: " + v.getEdadMinimaMeses() + " meses)");
        if (vacuna == null) {
            return;
        }
        final String lote = entrada.leerTexto("Número de lote de la vacuna");
        ejecutar(() -> {
            controlador.aplicarVacuna(consulta, vacuna, lote);
            System.out.println("Vacuna aplicada.");
        });
    }

    private void facturarPendiente() throws StockInsuficienteException, LoteVencidoException, IOException {
        Consulta consulta = entrada.elegir("Consultas pendientes de facturar", controlador.getConsultasSinFacturar(),
                c -> FECHA.format(c.getFecha()) + " - " + c.getTurno().getMascota().getNombre() + " - " + c.getMotivo());
        if (consulta != null) {
            facturarConsulta(consulta);
        }
    }

    private void facturarConsulta(Consulta consulta) throws StockInsuficienteException, LoteVencidoException, IOException {
        // Los insumos son los medicamentos de los tratamientos indicados
        Map<Producto, Integer> insumos = new LinkedHashMap<>();
        Set<Producto> consultados = new HashSet<>();
        for (Tratamiento t : consulta.getTratamientos()) {
            Producto producto = t.getMedicamento();
            if (consultados.add(producto)) {
                int unidades = entrada.leerEntero("Unidades de " + producto.getNombre()
                        + " utilizadas (0 = ninguna)", 0, 1000);
                if (unidades > 0) {
                    insumos.put(producto, unidades);
                }
            }
        }
        PoliticaDeCobro politica = pedirMedioDePago();
        Factura factura = controlador.facturar(consulta, insumos, politica);
        mostrarFactura(factura);
        for (Producto p : insumos.keySet()) {
            if (p.necesitaReposicion()) {
                System.out.println("ALERTA DE REPOSICIÓN: " + p.getNombre() + " llegó al stock mínimo (quedan "
                        + p.getStockActual() + ", mínimo " + p.getStockMinimo() + ").");
            }
        }
    }

    private PoliticaDeCobro pedirMedioDePago() {
        System.out.println("Medio de pago:");
        System.out.println("  1. Efectivo (10 % de descuento)");
        System.out.println("  2. Débito");
        System.out.println("  3. Crédito en cuotas (8 % de recargo)");
        int opcion = entrada.leerEntero("Elija una opción", 1, 3);
        if (opcion == 1) {
            return new PagoEfectivo();
        }
        if (opcion == 2) {
            return new PagoDebito();
        }
        return new PagoCreditoCuotas(entrada.leerEntero("Cantidad de cuotas", 1, 24));
    }

    private void anularFactura() throws IOException {
        List<Factura> vigentes = new ArrayList<>();
        for (Factura f : controlador.getFacturas()) {
            if (!f.estaAnulada()) {
                vigentes.add(f);
            }
        }
        Factura factura = entrada.elegir("Facturas vigentes", vigentes, this::resumir);
        if (factura == null) {
            return;
        }
        String motivo = entrada.leerTexto("Motivo de la anulación");
        NotaDeCredito nota = controlador.anularFactura(factura, motivo);
        System.out.println("Factura " + factura.getNumero() + " anulada. Nota de crédito por " + dinero(nota.getImporte())
                + ". La consulta puede volver a facturarse.");
    }

    private void verFacturas() {
        if (controlador.getFacturas().isEmpty()) {
            System.out.println("No hay facturas emitidas.");
        }
        for (Factura f : controlador.getFacturas()) {
            System.out.println("- " + resumir(f));
        }
    }

    private void rectificarConsulta() throws IOException {
        List<Consulta> consultas = new ArrayList<>();
        for (Mascota m : controlador.getMascotas()) {
            consultas.addAll(m.getHistoriaClinica().getConsultas());
        }
        Consulta consulta = entrada.elegir("Consultas", consultas,
                c -> FECHA.format(c.getFecha()) + " - " + c.getTurno().getMascota().getNombre() + " - " + c.getMotivo());
        if (consulta == null) {
            return;
        }
        Veterinario autor = entrada.elegir("Veterinario que firma la nota", controlador.getVeterinarios(),
                v -> v.getApellido() + ", " + v.getNombre() + " (Mat. " + v.getMatricula() + ")");
        if (autor == null) {
            return;
        }
        String texto = entrada.leerTexto("Texto de la rectificación");
        NotaRectificatoria nota = controlador.rectificarConsulta(consulta, autor, texto, LocalDate.now());
        System.out.println("Nota rectificatoria agregada a la historia clínica, firmada por " + nota.getFirma()
                + ". La consulta original no se modificó.");
    }

    private void verHistoriaClinica() {
        Mascota mascota = entrada.elegir("Mascotas", controlador.getMascotas(), MenuConsola::describir);
        if (mascota == null) {
            return;
        }
        List<Consulta> consultas = mascota.getHistoriaClinica().getConsultas();
        System.out.println("\nHistoria clínica de " + mascota.getNombre() + " (" + consultas.size() + " consultas)");
        for (Consulta c : consultas) {
            System.out.println("- " + FECHA.format(c.getFecha()) + " | " + c.getMotivo()
                    + (c.estaCerrada() ? " [confirmada]" : " [abierta]"));
            System.out.println("    Peso: " + (c.getPesoDelDia() == null ? "sin registrar" : c.getPesoDelDia() + " kg")
                    + " | Diagnóstico: " + (c.getDiagnostico() == null ? "sin registrar" : c.getDiagnostico()));
            for (Tratamiento t : c.getTratamientos()) {
                System.out.println("    Tratamiento: " + t.getMedicamento().getNombre());
            }
            for (VacunaAplicada v : c.getVacunas()) {
                System.out.println("    Vacuna: " + v.getVacuna().getNombre() + " (lote " + v.getLote() + ")");
            }
            for (NotaRectificatoria n : mascota.getHistoriaClinica().getNotasDe(c)) {
                System.out.println("    RECTIFICACIÓN " + FECHA.format(n.getFecha()) + ": " + n.getTexto()
                        + " - firmada por " + n.getFirma());
            }
        }
    }

    private void mostrarFactura(Factura factura) {
        System.out.println("\n===== FACTURA N° " + factura.getNumero() + " - " + FECHA.format(factura.getFecha()) + " =====");
        System.out.println("Mascota: " + factura.getConsulta().getTurno().getMascota().getNombre());
        for (LineaFactura l : factura.getLineas()) {
            System.out.println("  " + l.getDescripcion() + "  x" + l.getCantidad() + "  " + dinero(l.getPrecio())
                    + "  = " + dinero(l.getSubtotal()));
        }
        System.out.println("Medio de pago: " + describir(factura.getPoliticaDeCobro()));
        System.out.println("TOTAL: " + dinero(controlador.getTotal(factura)));
        System.out.println("=================================");
    }

    // =====================================================================
    // Dueños, mascotas y veterinarios
    // =====================================================================

    private void registrarDuenio() throws IOException {
        String nombre = entrada.leerTexto("Nombre");
        String apellido = entrada.leerTexto("Apellido");
        String dni = entrada.leerTexto("DNI");
        String telefono = entrada.leerTextoOpcional("Teléfono (Enter para omitir)");
        String email = entrada.leerTextoOpcional("Email (Enter para omitir)");
        PlanSalud plan = null;
        if (entrada.confirmar("¿Tiene plan de salud?")) {
            String nombrePlan = entrada.leerTexto("Nombre del plan");
            double descuento = entrada.leerPorcentaje("Descuento del plan (%)");
            plan = new PlanSalud(nombrePlan, descuento);
        }
        Duenio duenio = controlador.registrarDuenio(nombre, apellido, dni, telefono, email, plan);
        System.out.println("Dueño registrado: " + duenio.getNombre() + " " + duenio.getApellido());
    }

    private void registrarMascota() throws IOException {
        Duenio duenio = entrada.elegir("Dueños", controlador.getDuenios(), d -> d.getApellido() + ", " + d.getNombre());
        if (duenio == null) {
            return;
        }
        Especie especie = entrada.elegir("Especies", Arrays.asList(Especie.values()), Especie::name);
        if (especie == null) {
            return;
        }
        String nombre = entrada.leerTexto("Nombre de la mascota");
        double peso = entrada.leerDecimalPositivo("Peso actual (kg)");
        String raza = entrada.leerTexto("Raza");
        String sexo = entrada.leerTexto("Sexo (M/H)");
        LocalDate nacimiento = entrada.leerFecha("Fecha de nacimiento");

        Mascota mascota;
        switch (especie) {
            case PERRO:
                String chip = entrada.leerTexto("Número de chip");
                String tamanio = entrada.leerTexto("Tamaño (chico/mediano/grande)");
                mascota = controlador.registrarPerro(duenio, nombre, peso, raza, sexo, nacimiento, chip, tamanio);
                break;
            case GATO:
                Entorno entorno = entrada.elegir("Entornos", Arrays.asList(Entorno.values()), Entorno::name);
                if (entorno == null) {
                    return;
                }
                mascota = controlador.registrarGato(duenio, nombre, peso, raza, sexo, nacimiento, entorno);
                break;
            case AVE:
                boolean vuela = entrada.confirmar("¿Puede volar?");
                mascota = controlador.registrarAve(duenio, nombre, peso, raza, sexo, nacimiento, vuela);
                break;
            default:
                mascota = controlador.registrarConejo(duenio, nombre, peso, raza, sexo, nacimiento);
                break;
        }
        System.out.println("Mascota registrada: " + describir(mascota));
    }

    private void cambiarDuenio() throws IOException {
        Mascota mascota = entrada.elegir("Mascotas", controlador.getMascotas(), MenuConsola::describir);
        if (mascota == null) {
            return;
        }
        List<Duenio> otros = new ArrayList<>(controlador.getDuenios());
        otros.remove(mascota.getDuenio());
        Duenio nuevo = entrada.elegir("Nuevo dueño", otros, d -> d.getApellido() + ", " + d.getNombre());
        if (nuevo == null) {
            return;
        }
        controlador.cambiarDuenio(mascota, nuevo);
        System.out.println(mascota.getNombre() + " ahora pertenece a " + nuevo.getNombre() + " " + nuevo.getApellido()
                + ". Conserva su historia clínica.");
    }

    private void verDuenios() {
        if (controlador.getDuenios().isEmpty()) {
            System.out.println("No hay dueños registrados.");
        }
        for (Duenio d : controlador.getDuenios()) {
            System.out.println("\n" + d.descripcion());
            for (Mascota m : d.getMascotas()) {
                System.out.println("   - " + m.getNombre() + " (" + m.getEspecie() + ", " + m.getEdadEnMeses() + " meses, "
                        + m.getPesoActual() + " kg, " + m.getHistoriaClinica().getConsultas().size() + " consultas)");
            }
        }
    }

    private void verNotificaciones() {
        Duenio duenio = entrada.elegir("Dueños", controlador.getDuenios(), d -> d.getApellido() + ", " + d.getNombre());
        if (duenio == null) {
            return;
        }
        if (duenio.getNotificaciones().isEmpty()) {
            System.out.println("Sin notificaciones.");
        }
        for (Notificacion n : duenio.getNotificaciones()) {
            System.out.println("- " + FECHA_HORA.format(n.getFecha()) + ": " + n.getMensaje());
        }
    }

    private void designarGuardia() throws IOException {
        Veterinario v = entrada.elegir("Veterinarios", controlador.getVeterinarios(), MenuConsola::describirConHorarios);
        if (v == null) {
            return;
        }
        controlador.designarVeterinarioDeGuardia(v);
        System.out.println(v.getApellido() + " es ahora el veterinario de guardia (atiende las urgencias).");
    }

    private void registrarVeterinario() throws IOException {
        String nombre = entrada.leerTexto("Nombre");
        String apellido = entrada.leerTexto("Apellido");
        String dni = entrada.leerTexto("DNI");
        String telefono = entrada.leerTextoOpcional("Teléfono (Enter para omitir)");
        String matricula = entrada.leerTexto("Matrícula");
        String especialidad = entrada.leerTexto("Especialidad");
        Veterinario v = controlador.registrarVeterinario(nombre, apellido, dni, telefono, matricula, especialidad);
        System.out.println("Veterinario registrado: " + v.getApellido() + ", " + v.getNombre()
                + ". Falta cargar sus horarios de atención.");
    }

    private void cargarHorario() throws IOException {
        Veterinario v = entrada.elegir("Veterinarios", controlador.getVeterinarios(), MenuConsola::describirConHorarios);
        if (v == null) {
            return;
        }
        DayOfWeek dia = entrada.elegir("Días", Arrays.asList(DayOfWeek.values()), d -> nombreDia(d));
        if (dia == null) {
            return;
        }
        controlador.agregarHorario(v, dia, entrada.leerHora("Hora de inicio"), entrada.leerHora("Hora de fin"));
        System.out.println("Horario cargado.");
    }

    private void verVeterinarios() {
        if (controlador.getVeterinarios().isEmpty()) {
            System.out.println("No hay veterinarios registrados.");
        }
        for (Veterinario v : controlador.getVeterinarios()) {
            System.out.println("- " + describirConHorarios(v));
        }
    }

    // =====================================================================
    // Stock y configuración
    // =====================================================================

    private void registrarProducto() throws IOException {
        String codigo = entrada.leerTexto("Código");
        String nombre = entrada.leerTexto("Nombre");
        double precio = entrada.leerDecimalPositivo("Precio unitario");
        int minimo = entrada.leerEntero("Stock mínimo (alerta de reposición)", 0, 100000);
        Producto p = controlador.registrarProducto(codigo, nombre, precio, minimo);
        System.out.println("Producto registrado: " + p.getNombre() + ". Falta cargarle al menos un lote.");
    }

    private void agregarLote() throws IOException {
        Producto p = entrada.elegir("Productos", controlador.getProductos(), x -> x.getCodigo() + " - " + x.getNombre());
        if (p == null) {
            return;
        }
        String numero = entrada.leerTexto("Número de lote");
        int cantidad = entrada.leerEntero("Cantidad de unidades", 1, 100000);
        LocalDate vencimiento = entrada.leerFecha("Fecha de vencimiento");
        controlador.agregarLote(p, numero, cantidad, vencimiento);
        System.out.println("Lote agregado. Stock actual de " + p.getNombre() + ": " + p.getStockActual());
    }

    private void agregarProveedor() throws IOException {
        Producto p = entrada.elegir("Productos", controlador.getProductos(), x -> x.getCodigo() + " - " + x.getNombre());
        if (p == null) {
            return;
        }
        controlador.agregarProveedor(p, entrada.leerTexto("Código del proveedor"), entrada.leerTexto("Nombre del proveedor"));
        System.out.println("Proveedor agregado.");
    }

    private void verProductos() {
        if (controlador.getProductos().isEmpty()) {
            System.out.println("No hay productos registrados.");
        }
        LocalDate hoy = LocalDate.now();
        for (Producto p : controlador.getProductos()) {
            System.out.println("\n" + p.getCodigo() + " - " + p.getNombre() + " | " + dinero(p.getPrecio())
                    + " | stock: " + p.getStockActual() + " (mínimo " + p.getStockMinimo() + ")"
                    + (p.necesitaReposicion() ? "  << REPONER >>" : ""));
            for (LoteProducto l : p.getLotes()) {
                System.out.println("   lote " + l.getNumeroLote() + ": " + l.getCantidad() + " u. - vence "
                        + FECHA.format(l.getFechaVencimiento()) + (l.estaVencido(hoy) ? "  (VENCIDO, no se usa)" : ""));
            }
        }
    }

    private void definirPrecio() throws IOException {
        TipoAtencion tipo = entrada.elegir("Tipos de atención", Arrays.asList(TipoAtencion.values()), TipoAtencion::name);
        if (tipo == null) {
            return;
        }
        controlador.definirPrecioAtencion(tipo, entrada.leerDecimalPositivo("Precio"));
        System.out.println("Precio definido.");
    }

    private void verPrecios() {
        for (TipoAtencion tipo : TipoAtencion.values()) {
            String precio = controlador.getTarifario().tienePrecio(tipo)
                    ? dinero(controlador.getTarifario().getPrecio(tipo)) : "sin definir";
            System.out.println("- " + tipo + ": " + precio);
        }
    }

    private void registrarVacuna() throws IOException {
        String nombre = entrada.leerTexto("Nombre de la vacuna");
        int edadMinima = entrada.leerEntero("Edad mínima (meses)", 0, 240);
        int intervalo = entrada.leerEntero("Días hasta el próximo refuerzo", 1, 3650);
        List<Especie> especies = new ArrayList<>();
        for (Especie e : Especie.values()) {
            if (entrada.confirmar("¿Se aplica a " + e + "?")) {
                especies.add(e);
            }
        }
        controlador.registrarVacuna(nombre, edadMinima, intervalo, especies);
        System.out.println("Vacuna registrada en el catálogo.");
    }

    // =====================================================================
    // Reportes y datos de ejemplo
    // =====================================================================

    private void reporteTurnosPorVeterinario() {
        for (Map.Entry<Veterinario, List<Turno>> fila : controlador.getAgendaPorVeterinario().entrySet()) {
            System.out.println("\n" + fila.getKey().getApellido() + ", " + fila.getKey().getNombre()
                    + " (" + fila.getValue().size() + " turnos)");
            for (Turno t : fila.getValue()) {
                System.out.println("   - " + describir(t));
            }
        }
    }

    private void reporteVacunas() {
        List<AvisoVacuna> avisos = controlador.getAvisosDeVacunas(LocalDate.now());
        if (avisos.isEmpty()) {
            System.out.println("No hay vacunas para recordar ni vencidas.");
        }
        for (AvisoVacuna a : avisos) {
            System.out.println("- " + a.getMascota().getNombre() + " (dueño: " + a.getMascota().getDuenio().getApellido()
                    + "): " + a.getAplicacion().getVacuna().getNombre() + ", refuerzo el "
                    + FECHA.format(a.getAplicacion().getProximoRefuerzo())
                    + (a.isVencida() ? "  << VENCIDA >>" : "  (recordar)"));
        }
    }

    private void reporteStockBajo() {
        if (controlador.getProductosConStockBajo().isEmpty()) {
            System.out.println("Ningún producto está en el stock mínimo.");
        }
        for (Producto p : controlador.getProductosConStockBajo()) {
            System.out.println("- " + p.getNombre() + ": quedan " + p.getStockActual() + " (mínimo " + p.getStockMinimo() + ")");
        }
    }

    private void cargarEjemplo() throws IOException {
        DatosDeEjemplo.cargar(controlador);
        System.out.println("Datos de ejemplo cargados: 2 veterinarios (Gómez de guardia), 2 dueños, 4 mascotas, 3 productos, 4 vacunas y precios.");
    }

    // =====================================================================
    // Formato de salida
    // =====================================================================

    private static String describir(Mascota m) {
        Duenio d = m.getDuenio();
        String duenio = (d == null) ? "sin dueño" : d.getNombre() + " " + d.getApellido();
        return m.getNombre() + " (" + m.getEspecie() + ", dueño: " + duenio + ")";
    }

    private static String describir(Turno t) {
        return FECHA_HORA.format(t.getFechaHora()) + " - " + t.getMascota().getNombre() + " con "
                + t.getVeterinario().getApellido() + " - " + t.getTipoAtencion()
                + (t.esUrgencia() ? " [URGENCIA]" : "") + " [" + t.getEstado() + "]";
    }

    private static String describirConHorarios(Veterinario v) {
        StringBuilder horarios = new StringBuilder();
        for (modelo.HorarioAtencion h : v.getHorarios()) {
            horarios.append(nombreDia(h.getDia())).append(' ').append(h.getHoraInicio()).append('-')
                    .append(h.getHoraFin()).append("; ");
        }
        return v.getApellido() + ", " + v.getNombre() + " - " + v.getEspecialidad() + " (Mat. " + v.getMatricula() + ")"
                + (v.estaDeGuardia() ? " [DE GUARDIA]" : "") + " | "
                + (horarios.length() == 0 ? "sin horarios cargados" : horarios.toString());
    }

    private static String describir(PoliticaDeCobro p) {
        if (p instanceof PagoEfectivo) {
            return "Efectivo (10 % de descuento)";
        }
        if (p instanceof PagoCreditoCuotas) {
            return "Crédito en " + ((PagoCreditoCuotas) p).getCuotas() + " cuotas (8 % de recargo)";
        }
        return "Débito";
    }

    private String resumir(Factura f) {
        return "N° " + f.getNumero() + " - " + FECHA.format(f.getFecha()) + " - "
                + f.getConsulta().getTurno().getMascota().getNombre() + " - " + dinero(controlador.getTotal(f))
                + (f.estaAnulada() ? " [ANULADA]" : "");
    }

    private static String nombreDia(DayOfWeek dia) {
        return dia.getDisplayName(TextStyle.FULL, ES);
    }

    private static String dinero(double valor) {
        return String.format(ES, "$%.2f", valor);
    }
}
