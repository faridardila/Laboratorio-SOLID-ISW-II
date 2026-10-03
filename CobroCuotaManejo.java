import java.util.List;

public class CobroCuotaManejo {
    private static final double CUOTA = 12_900;
    private final NotificacionService notificacionService = new NotificacionService();

    public void cobrarMensual (List<Cuenta> cuentas) {
        for (Cuenta cuenta : cuentas) {
            cuenta.retirar (CUOTA);
            notificacionService.notificarCobroCuota(cuenta.getNumero());
        }
    }
}
