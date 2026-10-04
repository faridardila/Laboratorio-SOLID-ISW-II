import java.util.List;

public class NotificacionCompuestaService implements NotificacionService {
    private final List<NotificacionService> servicios;

    public NotificacionCompuestaService(NotificacionService... servicios) {
        this.servicios = List.of(servicios);
    }

    public NotificacionCompuestaService(List<NotificacionService> servicios) {
        this.servicios = servicios;
    }

    @Override
    public void notificarTransferencia(Cuenta origen, Cuenta destino, double monto) {
        for (NotificacionService servicio : servicios) {
            servicio.notificarTransferencia(origen, destino, monto);
        }
    }

    @Override
    public void notificarCobroCuota(String numeroCuenta) {
        for (NotificacionService servicio : servicios) {
            servicio.notificarCobroCuota(numeroCuenta);
        }
    }
}
