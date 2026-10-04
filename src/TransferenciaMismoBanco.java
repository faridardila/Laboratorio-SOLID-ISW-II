public class TransferenciaMismoBanco implements TipoTransferencia {
    @Override
    public String getNombre() {
        return "MISMO_BANCO";
    }

    @Override
    public double calcularComision(double monto) {
        return 0;
    }
}
