public class TransaccionService {
    private final ValidadorTransaccion validador = new ValidadorTransaccion();
    private final CalculadoraComision calculadoraComision = new CalculadoraComision();
    private final OracleRepositorio repositorio = new OracleRepositorio();
    private final ComprobanteService comprobanteService = new ComprobanteService();
    private final NotificacionService notificacionService = new NotificacionService();
    private final AuditoriaService auditoriaService = new AuditoriaService();

    public void transferir(Cuenta origen, Cuenta destino, double monto, String tipo) {
        validador.validar(monto);

        double comision = calculadoraComision.calcular(tipo, monto);

        origen.retirar(monto + comision);
        destino.depositar(monto);

        repositorio.guardarTransaccion(origen.getNumero(), destino.getNumero(), monto, comision);
        comprobanteService.imprimir(origen, destino, monto, comision);
        notificacionService.notificarTransferencia(origen, destino, monto);
        auditoriaService.registrar(tipo, origen, destino, monto);
    }
}
