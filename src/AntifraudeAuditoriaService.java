public class AntifraudeAuditoriaService implements AuditoriaService {
    @Override
    public void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
        System.out.println("[ANTIFRAUDE] Evaluacion de riesgo completada: " + tipo + " " + origen.getNumero() + " -> " + destino.getNumero() + " $" + monto);
    }
}
