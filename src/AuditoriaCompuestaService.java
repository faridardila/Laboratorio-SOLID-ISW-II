import java.util.List;

public class AuditoriaCompuestaService implements AuditoriaService {
    private final List<AuditoriaService> servicios;

    public AuditoriaCompuestaService(AuditoriaService... servicios) {
        this.servicios = List.of(servicios);
    }

    public AuditoriaCompuestaService(List<AuditoriaService> servicios) {
        this.servicios = servicios;
    }

    @Override
    public void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
        for (AuditoriaService servicio : servicios) {
            servicio.registrar(tipo, origen, destino, monto);
        }
    }
}
