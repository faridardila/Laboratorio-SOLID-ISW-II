public interface NotificacionService {
    void notificarTransferencia(Cuenta origen, Cuenta destino, double monto);
    void notificarCobroCuota(String numeroCuenta);
}
