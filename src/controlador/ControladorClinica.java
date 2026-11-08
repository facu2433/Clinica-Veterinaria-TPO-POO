package controlador;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Ave;
import modelo.CalculadorDeFactura;
import modelo.Conejo;
import modelo.Consulta;
import modelo.Duenio;
import modelo.Entorno;
import modelo.Especie;
import modelo.Estado;
import modelo.Factura;
import modelo.Gato;
import modelo.HorarioAtencion;
import modelo.LoteProducto;
import modelo.Mascota;
import modelo.NotaDeCredito;
import modelo.NotaRectificatoria;
import modelo.Perro;
import modelo.PlanSalud;
import modelo.Persona;
import modelo.PoliticaDeCobro;
import modelo.Producto;
import modelo.Proveedor;
import modelo.Reprogramacion;
import modelo.Senia;
import modelo.Tarifario;
import modelo.TipoAtencion;
import modelo.Tratamiento;
import modelo.Turno;
import modelo.Vacuna;
import modelo.VacunaAplicada;
import modelo.Veterinario;
import modelo.excepciones.EdadMinimaVacunaException;
import modelo.excepciones.LoteVencidoException;
import modelo.excepciones.PesoNoRegistradoException;
import modelo.excepciones.SeniaPendienteException;
import modelo.excepciones.StockInsuficienteException;
import modelo.excepciones.TurnoSuperpuestoException;
import persistencia.DatosClinica;
import persistencia.RepositorioClinica;

/**
 * Coordina: recibe pedidos de la vista, delega en el modelo y guarda.
 * No contiene reglas de negocio de las Partes A y B (esas viven en el modelo);
 * solo coordina objetos y cuida la integridad entre colecciones (DNI y códigos únicos,
 * una factura vigente por consulta, validar todo antes de descontar stock).
 *
 * Cada operación que modifica datos termina guardando en el repositorio.
 * Las excepciones del modelo (IllegalArgumentException, IllegalStateException y las
 * propias) se propagan: la vista decide qué mostrar.
 */
public class ControladorClinica {
    private final RepositorioClinica repositorio;
    private final CalculadorDeFactura calculador;
    private final DatosClinica datos;

    public ControladorClinica(RepositorioClinica repositorio, CalculadorDeFactura calculador)
            throws IOException, ClassNotFoundException {
        if (repositorio == null || calculador == null) {
            throw new IllegalArgumentException("El repositorio y el calculador son obligatorios.");
        }
        this.repositorio = repositorio;
        this.calculador = calculador;
        DatosClinica recuperados = repositorio.recuperar();
        this.datos = (recuperados != null) ? recuperados : new DatosClinica();
    }

    // =====================================================================
    // Altas
    // =====================================================================

    public Duenio registrarDuenio(String nombre, String apellido, String dni, String telefono,
                                  String email, PlanSalud plan) throws IOException {
        exigirDniLibre(dni);
        Duenio duenio = new Duenio(nombre, apellido, dni, telefono, email, plan);
        datos.getPersonas().add(duenio);
        guardar();
        return duenio;
    }

    public Veterinario registrarVeterinario(String nombre, String apellido, String dni, String telefono,
                                            String matricula, String especialidad) throws IOException {
        exigirDniLibre(dni);
        Veterinario veterinario = new Veterinario(nombre, apellido, dni, telefono, matricula, especialidad);
        datos.getPersonas().add(veterinario);
        guardar();
        return veterinario;
    }

    public void agregarHorario(Veterinario veterinario, DayOfWeek dia, LocalTime inicio, LocalTime fin)
            throws IOException {
        exigir(veterinario, "veterinario");
        veterinario.agregarHorario(new HorarioAtencion(dia, inicio, fin));
        guardar();
    }

    public Perro registrarPerro(Duenio duenio, String nombre, double peso, String raza, String sexo,
                                LocalDate nacimiento, String numeroChip, String tamanio) throws IOException {
        return incorporar(duenio, new Perro(nombre, peso, raza, sexo, nacimiento, numeroChip, tamanio));
    }

    public Gato registrarGato(Duenio duenio, String nombre, double peso, String raza, String sexo,
                              LocalDate nacimiento, Entorno entorno) throws IOException {
        return incorporar(duenio, new Gato(nombre, peso, raza, sexo, nacimiento, entorno));
    }

    public Ave registrarAve(Duenio duenio, String nombre, double peso, String raza, String sexo,
                            LocalDate nacimiento, boolean puedeVolar) throws IOException {
        return incorporar(duenio, new Ave(nombre, peso, raza, sexo, nacimiento, puedeVolar));
    }

    public Conejo registrarConejo(Duenio duenio, String nombre, double peso, String raza, String sexo,
                                  LocalDate nacimiento) throws IOException {
        return incorporar(duenio, new Conejo(nombre, peso, raza, sexo, nacimiento));
    }

    public void cambiarDuenio(Mascota mascota, Duenio nuevo) throws IOException {
        exigir(mascota, "mascota");
        mascota.cambiarDuenio(nuevo);
        guardar();
    }

    public Producto registrarProducto(String codigo, String nombre, double precio, int stockMinimo)
            throws IOException {
        for (Producto p : datos.getProductos()) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                throw new IllegalArgumentException("Ya existe un producto con el código " + codigo + ".");
            }
        }
        Producto producto = new Producto(codigo, nombre, precio, stockMinimo);
        datos.getProductos().add(producto);
        guardar();
        return producto;
    }

    public void agregarLote(Producto producto, String numeroLote, int cantidad, LocalDate vencimiento)
            throws IOException {
        exigir(producto, "producto");
        producto.agregarLote(new LoteProducto(numeroLote, cantidad, vencimiento));
        guardar();
    }

    public void agregarProveedor(Producto producto, String codigo, String nombre) throws IOException {
        exigir(producto, "producto");
        producto.agregarProveedor(new Proveedor(codigo, nombre));
        guardar();
    }

    public Vacuna registrarVacuna(String nombre, int edadMinimaMeses, int intervaloRefuerzoDias,
                                  List<Especie> especies) throws IOException {
        for (Vacuna v : datos.getVacunas()) {
            if (v.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe una vacuna llamada " + nombre + ".");
            }
        }
        Vacuna vacuna = new Vacuna(nombre, edadMinimaMeses, intervaloRefuerzoDias, especies);
        datos.getVacunas().add(vacuna);
        guardar();
        return vacuna;
    }

    public void definirPrecioAtencion(TipoAtencion tipo, double precio) throws IOException {
        datos.getTarifario().definirPrecio(tipo, precio);
        guardar();
    }

    // =====================================================================
    // Caso de uso 1: asignar turno (diagrama de secuencia "asignarTurno")
    // =====================================================================

    /**
     * Crea el turno y lo agenda en el veterinario, que valida horario y superposición.
     * Antes comprueba que el dueño no deba una seña (tres ausencias seguidas).
     * Si lanza una excepción no queda nada guardado (alt del diagrama).
     */
    public Turno asignarTurno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora,
                              TipoAtencion tipo)
            throws TurnoSuperpuestoException, SeniaPendienteException, IOException {
        exigir(mascota, "mascota");
        if (mascota.getDuenio() != null) {
            mascota.getDuenio().verificarPuedePedirTurno();
        }
        Turno turno = new Turno(mascota, veterinario, fechaHora, tipo);
        veterinario.agendarTurno(turno);
        datos.getTurnos().add(turno);
        guardar();
        return turno;
    }

    /**
     * Mueve un turno programado a otro horario del mismo veterinario (valida horario y
     * superposición como al asignar).
     */
    public void reprogramarTurno(Turno turno, LocalDateTime nuevaFechaHora)
            throws TurnoSuperpuestoException, IOException {
        exigir(turno, "turno");
        turno.getVeterinario().reprogramarTurno(turno, nuevaFechaHora);
        guardar();
    }

    /** Designa al veterinario de guardia (solo uno a la vez: quita la guardia a los demás). */
    public void designarVeterinarioDeGuardia(Veterinario veterinario) throws IOException {
        exigir(veterinario, "veterinario");
        for (Veterinario v : getVeterinarios()) {
            v.setDeGuardia(v == veterinario);
        }
        guardar();
    }

    /** El veterinario de guardia actual, o null si no hay ninguno designado. */
    public Veterinario getVeterinarioDeGuardia() {
        for (Veterinario v : getVeterinarios()) {
            if (v.estaDeGuardia()) {
                return v;
            }
        }
        return null;
    }

    /**
     * Atiende una urgencia: toma el primer horario del veterinario de guardia y, si está ocupado
     * por turnos comunes, los desplaza al siguiente horario libre y avisa a sus dueños.
     * Supuesto: una urgencia no exige seña (no se puede negar atención a un animal en riesgo).
     *
     * @param ahora momento en que llega la urgencia
     */
    public ResultadoUrgencia asignarUrgencia(Mascota mascota, TipoAtencion tipo, LocalDateTime ahora)
            throws TurnoSuperpuestoException, IOException {
        exigir(mascota, "mascota");
        exigir(tipo, "tipo de atención");
        Veterinario guardia = getVeterinarioDeGuardia();
        if (guardia == null) {
            throw new IllegalStateException(
                "No hay un veterinario de guardia designado (Veterinarios > Designar veterinario de guardia).");
        }
        LocalDateTime inicio = guardia.primerHorarioParaUrgencia(ahora, tipo.getDuracionMinutos());
        Turno urgencia = new Turno(mascota, guardia, inicio, tipo, true);
        List<Reprogramacion> desplazados = guardia.agendarUrgencia(urgencia);
        datos.getTurnos().add(urgencia);
        for (Reprogramacion r : desplazados) {
            Duenio duenio = r.getTurno().getMascota().getDuenio();
            if (duenio != null) {
                duenio.notificar(ahora, r.mensajeParaElDuenio());
            }
        }
        guardar();
        return new ResultadoUrgencia(urgencia, desplazados);
    }

    /**
     * Cancela un turno programado.
     *
     * @return el cargo por cancelación tardía (20 % del precio de la consulta); 0 si no corresponde.
     */
    public double cancelarTurno(Turno turno, LocalDateTime ahora) throws IOException {
        exigir(turno, "turno");
        TipoAtencion tipo = turno.getTipoAtencion();
        // Se comprueba ANTES de cancelar para no dejar el turno cancelado si después no se puede calcular el cargo.
        if (!datos.getTarifario().tienePrecio(tipo)) {
            throw new IllegalStateException("Antes de cancelar turnos hay que definir el precio de "
                    + tipo + " (Configuración > Precios de atención).");
        }
        boolean hayCargo = turno.cancelar(ahora);
        double cargo = hayCargo
                ? calculador.calcularCargoCancelacion(datos.getTarifario().getPrecio(tipo))
                : 0;
        guardar();
        return cargo;
    }

    /**
     * Marca la ausencia (faltó sin avisar). El horario queda libre.
     *
     * @return las ausencias seguidas que lleva ahora el dueño (con 3 debe pagar una seña)
     */
    public int marcarAusente(Turno turno) throws IOException {
        exigir(turno, "turno");
        turno.marcarAusente();
        guardar();
        Duenio duenio = turno.getMascota().getDuenio();
        return (duenio == null) ? 0 : duenio.getAusenciasSeguidas();
    }

    // ---- Seña por ausencias --------------------------------------------------

    public boolean requiereSenia(Duenio duenio) {
        exigir(duenio, "dueño");
        return duenio.requiereSenia();
    }

    /** Monto de la seña (50 % de la consulta general); requiere el precio definido. */
    public double getMontoSenia() {
        Tarifario tarifa = datos.getTarifario();
        if (!tarifa.tienePrecio(TipoAtencion.CONSULTA_GENERAL)) {
            throw new IllegalStateException("Falta definir el precio de " + TipoAtencion.CONSULTA_GENERAL
                    + " (Configuración > Precios de atención).");
        }
        return calculador.calcularSenia(tarifa.getPrecio(TipoAtencion.CONSULTA_GENERAL));
    }

    /** Registra el pago de la seña de un dueño con tres ausencias seguidas. */
    public Senia registrarSenia(Duenio duenio, LocalDate fecha) throws IOException {
        exigir(duenio, "dueño");
        if (!duenio.requiereSenia()) {
            throw new IllegalStateException("El dueño no tiene una seña pendiente.");
        }
        Senia senia = duenio.registrarSenia(getMontoSenia(), fecha);
        guardar();
        return senia;
    }

    // =====================================================================
    // Caso de uso 2: registrar consulta (diagrama de secuencia "registrarConsulta")
    // =====================================================================

    /** Crea la consulta en la historia clínica de la mascota y pasa el turno a ATENDIDO. */
    public Consulta registrarConsulta(Turno turno, String motivo) throws IOException {
        exigir(turno, "turno");
        Consulta consulta = turno.getMascota().getHistoriaClinica().registrarConsulta(turno, motivo);
        guardar();
        return consulta;
    }

    public void registrarPeso(Consulta consulta, double peso) throws IOException {
        exigir(consulta, "consulta");
        consulta.registrarPeso(peso);
        guardar();
    }

    /** El autor es el veterinario del turno: solo él atiende la consulta. */
    public void registrarDiagnostico(Consulta consulta, String diagnostico) throws IOException {
        exigir(consulta, "consulta");
        consulta.registrarDiagnostico(consulta.getTurno().getVeterinario(), diagnostico);
        guardar();
    }

    public void indicarTratamiento(Consulta consulta, Producto medicamento, double dosis,
                                   String frecuencia, int duracionDias)
            throws PesoNoRegistradoException, IOException {
        exigir(consulta, "consulta");
        consulta.indicarTratamiento(consulta.getTurno().getVeterinario(),
                new Tratamiento(medicamento, dosis, frecuencia, duracionDias));
        guardar();
    }

    public void aplicarVacuna(Consulta consulta, Vacuna vacuna, String lote)
            throws EdadMinimaVacunaException, IOException {
        exigir(consulta, "consulta");
        consulta.aplicarVacuna(vacuna, lote);
        guardar();
    }

    /**
     * Corrige una consulta sin modificarla: agrega a la historia clínica una nota
     * rectificatoria firmada por el veterinario indicado.
     */
    public NotaRectificatoria rectificarConsulta(Consulta consulta, Veterinario autor, String texto,
                                                 LocalDate fecha) throws IOException {
        exigir(consulta, "consulta");
        exigir(autor, "veterinario que firma");
        NotaRectificatoria nota = consulta.getTurno().getMascota().getHistoriaClinica()
                .rectificar(consulta, autor, texto, fecha);
        guardar();
        return nota;
    }

    // =====================================================================
    // Facturación
    // =====================================================================

    /**
     * Confirma la consulta: descuenta el stock de los insumos y emite la factura.
     * Es todo o nada: primero se valida TODO (consulta sin factura vigente, precios
     * definidos, stock de cada insumo) y recién después se modifica algo.
     *
     * @param insumos producto -> unidades utilizadas (puede ser vacío)
     */
    public Factura facturar(Consulta consulta, Map<Producto, Integer> insumos, PoliticaDeCobro politica)
            throws StockInsuficienteException, LoteVencidoException, IOException {
        exigir(consulta, "consulta");
        exigir(politica, "medio de pago");
        if (tieneFacturaVigente(consulta)) {
            throw new IllegalStateException("La consulta ya tiene una factura. "
                    + "Si hay un error, anúlela con una nota de crédito.");
        }
        Tarifario tarifa = datos.getTarifario();
        TipoAtencion tipo = consulta.getTurno().getTipoAtencion();
        if (!tarifa.tienePrecio(tipo)) {
            throw new IllegalStateException("Falta definir el precio de " + tipo
                    + " (Configuración > Precios de atención).");
        }
        Map<Producto, Integer> usados = (insumos != null) ? insumos : new LinkedHashMap<Producto, Integer>();

        // 1) Validar todo sin modificar nada
        for (Map.Entry<Producto, Integer> uso : usados.entrySet()) {
            Producto producto = uso.getKey();
            Integer cantidad = uso.getValue();
            if (producto == null || cantidad == null) {
                throw new IllegalArgumentException("Cada insumo necesita un producto y una cantidad.");
            }
            if (!producto.tienePrecio()) {
                throw new IllegalStateException("El producto " + producto.getNombre() + " no tiene precio definido.");
            }
            producto.verificarStock(cantidad);
        }

        // 2) Recién ahora se modifica: stock y factura
        Factura factura = new Factura(consulta, politica);
        factura.agregarLineaConsulta(tarifa);
        for (Map.Entry<Producto, Integer> uso : usados.entrySet()) {
            uso.getKey().descontarStock(uso.getValue());
            factura.agregarLineaInsumo(uso.getKey(), uso.getValue());
        }
        factura.emitir(datos.getFacturas().size() + 1, calculador);
        datos.getFacturas().add(factura);
        consulta.cerrar();   // confirmada: desde ahora solo se corrige con una nota rectificatoria
        guardar();
        return factura;
    }

    /** Anula una factura emitida generando su nota de crédito. */
    public NotaDeCredito anularFactura(Factura factura, String motivo) throws IOException {
        exigir(factura, "factura");
        NotaDeCredito nota = factura.anular(motivo);
        guardar();
        return nota;
    }

    public double getTotal(Factura factura) {
        return factura.getTotal(calculador);
    }

    // =====================================================================
    // Consultas para la vista (listas de solo lectura, ordenadas con Comparator)
    // =====================================================================

    public List<Duenio> getDuenios() {
        List<Duenio> resultado = new ArrayList<>();
        for (Persona p : datos.getPersonas()) {
            if (p instanceof Duenio) {
                resultado.add((Duenio) p);
            }
        }
        return resultado;
    }

    public List<Veterinario> getVeterinarios() {
        List<Veterinario> resultado = new ArrayList<>();
        for (Persona p : datos.getPersonas()) {
            if (p instanceof Veterinario) {
                resultado.add((Veterinario) p);
            }
        }
        return resultado;
    }

    public List<Mascota> getMascotas() {
        return Collections.unmodifiableList(datos.getMascotas());
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(datos.getProductos());
    }

    public List<Vacuna> getVacunas() {
        return Collections.unmodifiableList(datos.getVacunas());
    }

    public List<Factura> getFacturas() {
        return Collections.unmodifiableList(datos.getFacturas());
    }

    public Tarifario getTarifario() {
        return datos.getTarifario();
    }

    /** Turnos que todavía no se atendieron ni cancelaron, del más próximo al más lejano. */
    public List<Turno> getTurnosProgramados() {
        List<Turno> resultado = new ArrayList<>();
        for (Turno t : datos.getTurnos()) {
            if (t.getEstado() == Estado.PROGRAMADO) {
                resultado.add(t);
            }
        }
        Collections.sort(resultado, Comparator.comparing(Turno::getFechaHora));
        return resultado;
    }

    /** Consultas registradas que todavía no tienen factura vigente. */
    public List<Consulta> getConsultasSinFacturar() {
        List<Consulta> resultado = new ArrayList<>();
        for (Mascota m : datos.getMascotas()) {
            for (Consulta c : m.getHistoriaClinica().getConsultas()) {
                if (!tieneFacturaVigente(c)) {
                    resultado.add(c);
                }
            }
        }
        Collections.sort(resultado, Comparator.comparing(Consulta::getFecha));
        return resultado;
    }

    public boolean tieneDatos() {
        return !datos.getPersonas().isEmpty() || !datos.getProductos().isEmpty();
    }

    // =====================================================================
    // Reportes del administrador
    // =====================================================================

    /** Turnos por veterinario (su agenda), ordenados por fecha. Incluye veterinarios sin turnos. */
    public Map<Veterinario, List<Turno>> getAgendaPorVeterinario() {
        Map<Veterinario, List<Turno>> resultado = new LinkedHashMap<>();
        for (Veterinario v : getVeterinarios()) {
            List<Turno> agenda = new ArrayList<>(v.getAgenda());
            Collections.sort(agenda, Comparator.comparing(Turno::getFechaHora));
            resultado.put(v, agenda);
        }
        return resultado;
    }

    /**
     * Vacunas que hay que recordar o que ya vencieron. Para cada mascota y cada vacuna
     * se mira solo la aplicación más reciente: si ya se aplicó el refuerzo, la anterior no cuenta.
     */
    public List<AvisoVacuna> getAvisosDeVacunas(LocalDate hoy) {
        List<AvisoVacuna> avisos = new ArrayList<>();
        for (Mascota mascota : datos.getMascotas()) {
            Map<Vacuna, VacunaAplicada> ultimas = new LinkedHashMap<>();
            for (Consulta consulta : mascota.getHistoriaClinica().getConsultas()) {
                for (VacunaAplicada aplicada : consulta.getVacunas()) {
                    VacunaAplicada anterior = ultimas.get(aplicada.getVacuna());
                    if (anterior == null || aplicada.getFecha().isAfter(anterior.getFecha())) {
                        ultimas.put(aplicada.getVacuna(), aplicada);
                    }
                }
            }
            for (VacunaAplicada aplicada : ultimas.values()) {
                if (aplicada.estaVencida(hoy)) {
                    avisos.add(new AvisoVacuna(mascota, aplicada, true));
                } else if (aplicada.necesitaRecordatorio(hoy)) {
                    avisos.add(new AvisoVacuna(mascota, aplicada, false));
                }
            }
        }
        Collections.sort(avisos, Comparator.comparing(a -> a.getAplicacion().getProximoRefuerzo()));
        return avisos;
    }

    public List<Producto> getProductosConStockBajo() {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : datos.getProductos()) {
            if (p.necesitaReposicion()) {
                resultado.add(p);
            }
        }
        Collections.sort(resultado, Comparator.comparing(Producto::getNombre));
        return resultado;
    }

    // =====================================================================
    // Persistencia
    // =====================================================================

    public void guardarTodo() throws IOException {
        guardar();
    }

    public DatosClinica getDatos() {
        return datos;
    }

    // =====================================================================
    // Internos
    // =====================================================================

    private void guardar() throws IOException {
        repositorio.guardar(datos);
    }

    private <M extends Mascota> M incorporar(Duenio duenio, M mascota) throws IOException {
        exigir(duenio, "dueño");
        duenio.agregarMascota(mascota);
        datos.getMascotas().add(mascota);
        guardar();
        return mascota;
    }

    private boolean tieneFacturaVigente(Consulta consulta) {
        for (Factura f : datos.getFacturas()) {
            if (f.getConsulta() == consulta && !f.estaAnulada()) {
                return true;
            }
        }
        return false;
    }

    private void exigirDniLibre(String dni) {
        for (Persona p : datos.getPersonas()) {
            if (p.getDNI().equals(dni)) {
                throw new IllegalArgumentException("Ya existe una persona con el DNI " + dni + ".");
            }
        }
    }

    private void exigir(Object objeto, String nombre) {
        if (objeto == null) {
            throw new IllegalArgumentException("Falta indicar: " + nombre + ".");
        }
    }
}
