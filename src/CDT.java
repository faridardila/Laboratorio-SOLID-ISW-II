import java.time.LocalDate;

public class CDT extends Cuenta {
    private final LocalDate vencimiento;

    public CDT (String numero, String titular, double monto, LocalDate vencimiento) {
        super (numero, titular, monto);
        this.vencimiento = vencimiento;
    }

    public void retirar (double monto) {
        if (LocalDate.now().isBefore(vencimiento)) {
            throw new UnsupportedOperationException("Un CDT no permite retiros antes del vencimiento");
        }
        if (monto > saldo) throw new IllegalStateException("Saldo insuficiente");
        saldo -= monto;
    }

    public LocalDate getVencimiento() {
        return vencimiento;
    }
}
