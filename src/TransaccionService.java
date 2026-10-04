public class TransaccionService {
    private final ValidadorTransaccion validador;
    private final TransaccionRepositorio repositorio;
    private final ComprobanteService comprobanteService;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;

    public TransaccionService(
            ValidadorTransaccion validador,
            TransaccionRepositorio repositorio,
            ComprobanteService comprobanteService,
            NotificacionService notificacionService,
            AuditoriaService auditoriaService
    ) {
        this.validador = validador;
        this.repositorio = repositorio;
        this.comprobanteService = comprobanteService;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
    }

    public void transferir(CuentaOperativa origen, Cuenta destino, double monto, TipoTransferencia tipo) {
        validador.validar(monto);

        double comision = tipo.calcularComision(monto);

        origen.retirar(monto + comision);
        destino.depositar(monto);

        repositorio.guardarTransaccion(origen.getNumero(), destino.getNumero(), monto, comision);
        comprobanteService.imprimir(origen, destino, monto, comision);
        notificacionService.notificarTransferencia(origen, destino, monto);
        auditoriaService.registrar(tipo.getNombre(), origen, destino, monto);
    }
}
