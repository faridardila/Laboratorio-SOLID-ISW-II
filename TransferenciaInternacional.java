public class TransferenciaInternacional implements TipoTransferencia {
    @Override
    public String getNombre() {
        return "INTERNACIONAL";
    }

    @Override
    public double calcularComision(double monto) {
        return monto * 0.03 + 25_000;
    }
}
