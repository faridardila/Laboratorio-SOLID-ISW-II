import java.util.List;

public class CobroCuotaManejo {
    private static final double CUOTA = 12_900;
    private final NotificacionService notificacionService;

    public CobroCuotaManejo(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    public CobroCuotaManejo() {
        this(new SmsNotificacionService());
    }

    public void cobrarMensual (List<? extends CuentaOperativa> cuentas) {
        for (CuentaOperativa cuenta : cuentas) {
            cuenta.retirar (CUOTA);
            notificacionService.notificarCobroCuota(cuenta.getNumero());
        }
    }
}
