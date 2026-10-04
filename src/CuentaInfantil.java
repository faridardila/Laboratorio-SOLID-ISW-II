public class CuentaInfantil extends CuentaOperativa {
    private static final double TOPE_DIARIO_RETIRO = 200_000;
    private double totalRetiradoHoy = 0;

    public CuentaInfantil(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    @Override
    public void retirar(double monto) {
        if (totalRetiradoHoy + monto > TOPE_DIARIO_RETIRO) {
            throw new IllegalStateException("Supera el tope diario de retiro de $" + TOPE_DIARIO_RETIRO);
        }
        super.retirar(monto);
        totalRetiradoHoy += monto;
    }

    public double getTotalRetiradoHoy() {
        return totalRetiradoHoy;
    }
}
