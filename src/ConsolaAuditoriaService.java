import java.time.LocalDateTime;

public class ConsolaAuditoriaService implements AuditoriaService {
    @Override
    public void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
        System.out.println("[AUDITORIA] " + LocalDateTime.now() + " " + tipo + " " + origen.getNumero() + " -> " + destino.getNumero() + " $" + monto);
    }
}
