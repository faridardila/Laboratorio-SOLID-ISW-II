public interface AuditoriaService {
    void registrar(String tipo, Cuenta origen, Cuenta destino, double monto);
}
