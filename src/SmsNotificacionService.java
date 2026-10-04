public class SmsNotificacionService implements NotificacionService {
    private final SmsGateway sms;

    public SmsNotificacionService() {
        this.sms = new SmsGateway();
    }

    public SmsNotificacionService(SmsGateway sms) {
        this.sms = sms;
    }

    @Override
    public void notificarTransferencia(Cuenta origen, Cuenta destino, double monto) {
        sms.enviar(origen.getTitular(), "Transferiste $" + monto + " a la cuenta " + destino.getNumero());
    }

    @Override
    public void notificarCobroCuota(String numeroCuenta) {
        System.out.println("Cuota de manejo cobrada a " + numeroCuenta);
    }
}
