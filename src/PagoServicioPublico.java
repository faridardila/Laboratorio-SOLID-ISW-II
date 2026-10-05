public class PagoServicioPublico implements TipoTransferencia {
    @Override
    public String getNombre() {
        return "PAGO_SERVICIO_PUBLICO";
    }

    @Override
    public double calcularComision(double monto) {
        return 1_500;
    }
}
