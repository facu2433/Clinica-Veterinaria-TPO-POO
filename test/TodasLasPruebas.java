/** Corre todas las clases de prueba juntas. */
public class TodasLasPruebas {

    public static void main(String[] args) {
        PruebasAgenda.ejecutar();
        PruebasClinica.ejecutar();
        PruebasFacturacion.ejecutar();
        PruebasControlador.ejecutar();
        Prueba.terminar();
    }
}
