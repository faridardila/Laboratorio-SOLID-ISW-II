public class PushGateway {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[PUSH] Conectando al servicio de notificaciones push...");
        System.out.println("[PUSH] Para " + destinatario + ": " + mensaje);
    }
}
