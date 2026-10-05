public class PagoServiciosService {
    private final TransaccionService transaccionService;
    private final TipoTransferencia tipoPago = new PagoServicioPublico();

    public PagoServiciosService(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    public void pagar(CuentaOperativa origen, FacturaServicioPublico factura, double valor) {
        transaccionService.transferir(origen, factura, valor, tipoPago);
    }
}
