public class PushNotificacionService implements NotificacionService {
    private final PushGateway push;

    public PushNotificacionService() {
        this.push = new PushGateway();
    }

    public PushNotificacionService(PushGateway push) {
        this.push = push;
    }

    @Override
    public void notificarTransferencia(Cuenta origen, Cuenta destino, double monto) {
        push.enviar(origen.getTitular(), "Transferiste $" + monto + " a la cuenta " + destino.getNumero());
    }

    @Override
    public void notificarCobroCuota(String numeroCuenta) {
        System.out.println("Cuota de manejo cobrada a " + numeroCuenta);
    }
}
