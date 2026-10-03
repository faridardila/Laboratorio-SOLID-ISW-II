public abstract class CuentaOperativa extends Cuenta {
    public CuentaOperativa(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    public void retirar(double monto) {
        if (monto > saldo) throw new IllegalStateException("Saldo insuficiente");
        saldo -= monto;
    }
}
