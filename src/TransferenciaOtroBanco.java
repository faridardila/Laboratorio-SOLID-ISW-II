public class TransferenciaOtroBanco implements TipoTransferencia {
    @Override
    public String getNombre() {
        return "OTRO_BANCO";
    }

    @Override
    public double calcularComision(double monto) {
        return 7_500;
    }
}
