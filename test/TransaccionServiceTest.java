import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class TransaccionServiceTest {

    private FakeTransaccionRepositorio fakeRepositorio;
    private FakeNotificacionService fakeNotificacion;
    private DummyComprobanteService dummyComprobante;
    private DummyAuditoriaService dummyAuditoria;
    private ValidadorTransaccion validador;
    private TransaccionService servicio;

    private CuentaOperativa cuentaOrigen;
    private Cuenta cuentaDestino;

    @BeforeEach
    void setUp() {
        fakeRepositorio = new FakeTransaccionRepositorio();
        fakeNotificacion = new FakeNotificacionService();
        dummyComprobante = new DummyComprobanteService();
        dummyAuditoria = new DummyAuditoriaService();
        validador = new ValidadorMontoTope();

        servicio = new TransaccionService(
                validador,
                fakeRepositorio,
                dummyComprobante,
                fakeNotificacion,
                dummyAuditoria
        );

        cuentaOrigen = new CuentaAhorros("001", "Ana", 100_000);
        cuentaDestino = new CuentaAhorros("002", "Luis", 50_000);
    }

    @Test
    void testMismoBanco() {
        servicio.transferir(cuentaOrigen, cuentaDestino, 30_000, new TransferenciaMismoBanco());

        assertEquals(70_000, cuentaOrigen.getSaldo(), 0.001);
        assertEquals(80_000, cuentaDestino.getSaldo(), 0.001);
        assertEquals(1, fakeRepositorio.getTransacciones().size());
        assertEquals(0.0, fakeRepositorio.getTransacciones().get(0).comision, 0.001);
    }

    @Test
    void testTransferenciaLlave() {
        // Criterio de aceptación R1: una transferencia de tipo LLAVE por $50.000 descuenta exactamente $50.000 de la cuenta de origen.
        servicio.transferir(cuentaOrigen, cuentaDestino, 50_000, new TransferenciaLlave());

        assertEquals(50_000, cuentaOrigen.getSaldo(), 0.001); // 100.000 - 50.000 = 50.000 (sin comisión)
        assertEquals(100_000, cuentaDestino.getSaldo(), 0.001); // 50.000 + 50.000 = 100.000
        assertEquals(1, fakeRepositorio.getTransacciones().size());
        assertEquals(0.0, fakeRepositorio.getTransacciones().get(0).comision, 0.001);
    }

    @Test
    void testCuentaInfantilTopeRetiro() {
        // Criterio de aceptación R2: si la cuenta ya retiró $150.000 hoy, un retiro de $60.000 se rechaza y el saldo no cambia.
        CuentaInfantil cuentaInfantil = new CuentaInfantil("INF-01", "Pepito", 300_000);

        cuentaInfantil.retirar(150_000);
        assertEquals(150_000, cuentaInfantil.getSaldo(), 0.001);
        assertEquals(150_000, cuentaInfantil.getTotalRetiradoHoy(), 0.001);

        // Intento de retirar $60.000 adicionales (150.000 + 60.000 = 210.000 > 200.000 tope)
        assertThrows(IllegalStateException.class, () -> cuentaInfantil.retirar(60_000));

        // El saldo debe permanecer inalterado en 150.000
        assertEquals(150_000, cuentaInfantil.getSaldo(), 0.001);
    }

    @Test
    void testCuentaInfantilComoOrigenTransferenciaYCuotaManejo() {
        CuentaInfantil cuentaInfantil = new CuentaInfantil("INF-02", "Juanita", 250_000);

        // Usar como origen de transferencia
        servicio.transferir(cuentaInfantil, cuentaDestino, 50_000, new TransferenciaLlave());
        assertEquals(200_000, cuentaInfantil.getSaldo(), 0.001);

        // Cobro de cuota de manejo como cualquier cuenta operativa
        new CobroCuotaManejo(fakeNotificacion).cobrarMensual(List.of(cuentaInfantil));
        assertEquals(200_000 - 12_900, cuentaInfantil.getSaldo(), 0.001);
    }

    @Test
    void testNotificacionCompuestaSmsYPush() {
        // Criterio de aceptación R3: por cada transferencia exitosa se notifica por SMS y por PUSH
        FakeNotificacionService canalSms = new FakeNotificacionService();
        FakeNotificacionService canalPush = new FakeNotificacionService();

        NotificacionService compuesto = new NotificacionCompuestaService(canalSms, canalPush);

        TransaccionService servicioConCompuesto = new TransaccionService(
                validador,
                fakeRepositorio,
                dummyComprobante,
                compuesto,
                dummyAuditoria
        );

        servicioConCompuesto.transferir(cuentaOrigen, cuentaDestino, 20_000, new TransferenciaMismoBanco());

        // Ambos canales deben haber recibido 1 notificación de transferencia
        assertEquals(1, canalSms.getNotificacionesEnviadas());
        assertEquals(1, canalPush.getNotificacionesEnviadas());
    }

    @Test
    void testSistemaAntifraudeTransaccionExitosa() {
        // Criterio de aceptación R4: por cada transferencia exitosa aparecen auditoría y antifraude
        FakeAuditoriaService auditoriaEstandar = new FakeAuditoriaService();
        FakeAuditoriaService antifraude = new FakeAuditoriaService();

        AuditoriaService auditoriaCompuesta = new AuditoriaCompuestaService(auditoriaEstandar, antifraude);

        TransaccionService servicioConAntifraude = new TransaccionService(
                validador,
                fakeRepositorio,
                dummyComprobante,
                fakeNotificacion,
                auditoriaCompuesta
        );

        servicioConAntifraude.transferir(cuentaOrigen, cuentaDestino, 25_000, new TransferenciaMismoBanco());

        // Ambos servicios deben haber registrado la transacción exitosa
        assertEquals(1, auditoriaEstandar.getRegistros());
        assertEquals(1, antifraude.getRegistros());
    }

    @Test
    void testSistemaAntifraudeTransaccionRechazada() {
        // Criterio de aceptación R4: una transferencia rechazada no genera ninguno
        FakeAuditoriaService auditoriaEstandar = new FakeAuditoriaService();
        FakeAuditoriaService antifraude = new FakeAuditoriaService();

        AuditoriaService auditoriaCompuesta = new AuditoriaCompuestaService(auditoriaEstandar, antifraude);

        TransaccionService servicioConAntifraude = new TransaccionService(
                validador,
                fakeRepositorio,
                dummyComprobante,
                fakeNotificacion,
                auditoriaCompuesta
        );

        // Intento con monto inválido (> 5.000.000)
        assertThrows(IllegalArgumentException.class, () ->
                servicioConAntifraude.transferir(cuentaOrigen, cuentaDestino, 6_000_000, new TransferenciaMismoBanco())
        );

        // Ninguno debe registrar nada
        assertEquals(0, auditoriaEstandar.getRegistros());
        assertEquals(0, antifraude.getRegistros());
    }

    @Test
    void testOtroBanco() {
        servicio.transferir(cuentaOrigen, cuentaDestino, 30_000, new TransferenciaOtroBanco());

        // Comisión esperada = 7.500 -> Total debitado: 30.000 + 7.500 = 37.500
        // Saldo restante: 100.000 - 37.500 = 62.500
        assertEquals(62_500, cuentaOrigen.getSaldo(), 0.001);
        assertEquals(80_000, cuentaDestino.getSaldo(), 0.001);
        assertEquals(1, fakeRepositorio.getTransacciones().size());
        assertEquals(7_500.0, fakeRepositorio.getTransacciones().get(0).comision, 0.001);
    }

    @Test
    void testSaldoInsuficiente() {
        CuentaOperativa cuentaConPocoSaldo = new CuentaAhorros("003", "Carlos", 20_000);

        // Se intenta transferir 30.000 + 7.500 de comisión con saldo de 20.000
        assertThrows(IllegalStateException.class, () ->
                servicio.transferir(cuentaConPocoSaldo, cuentaDestino, 30_000, new TransferenciaOtroBanco())
        );

        // Los saldos no deben cambiar
        assertEquals(20_000, cuentaConPocoSaldo.getSaldo(), 0.001);
        assertEquals(50_000, cuentaDestino.getSaldo(), 0.001);

        // No se debe persistir ni notificar
        assertEquals(0, fakeRepositorio.getTransaccionesGuardadas());
        assertEquals(0, fakeNotificacion.getNotificacionesEnviadas());
    }

    @Test
    void testGuardadoYNotificacion() {
        servicio.transferir(cuentaOrigen, cuentaDestino, 20_000, new TransferenciaMismoBanco());

        assertEquals(1, fakeRepositorio.getTransaccionesGuardadas());
        assertEquals(1, fakeNotificacion.getNotificacionesEnviadas());
    }

    @Test
    void testTipoDesconocido() {
        TipoTransferencia tipoDesconocido = new TipoTransferencia() {
            @Override
            public String getNombre() {
                return "DESCONOCIDO";
            }

            @Override
            public double calcularComision(double monto) {
                throw new IllegalArgumentException("Tipo de transferencia desconocido");
            }
        };

        assertThrows(IllegalArgumentException.class, () ->
                servicio.transferir(cuentaOrigen, cuentaDestino, 20_000, tipoDesconocido)
        );

        // Saldo de origen no debe cambiar
        assertEquals(100_000, cuentaOrigen.getSaldo(), 0.001);
        assertEquals(50_000, cuentaDestino.getSaldo(), 0.001);
        assertEquals(0, fakeRepositorio.getTransaccionesGuardadas());
        assertEquals(0, fakeNotificacion.getNotificacionesEnviadas());
    }

    // ==========================================
    // DOBLES DE PRUEBA (Test Doubles / Fakes)
    // ==========================================

    static class RegistroTransaccion {
        String origen;
        String destino;
        double monto;
        double comision;

        RegistroTransaccion(String origen, String destino, double monto, double comision) {
            this.origen = origen;
            this.destino = destino;
            this.monto = monto;
            this.comision = comision;
        }
    }

    static class FakeTransaccionRepositorio implements TransaccionRepositorio {
        private final List<RegistroTransaccion> transacciones = new ArrayList<>();

        @Override
        public void guardarTransaccion(String origen, String destino, double monto, double comision) {
            transacciones.add(new RegistroTransaccion(origen, destino, monto, comision));
        }

        public int getTransaccionesGuardadas() {
            return transacciones.size();
        }

        public List<RegistroTransaccion> getTransacciones() {
            return transacciones;
        }
    }

    static class FakeNotificacionService implements NotificacionService {
        private int notificacionesTransferencia = 0;
        private int notificacionesCobro = 0;

        @Override
        public void notificarTransferencia(Cuenta origen, Cuenta destino, double monto) {
            notificacionesTransferencia++;
        }

        @Override
        public void notificarCobroCuota(String numeroCuenta) {
            notificacionesCobro++;
        }

        public int getNotificacionesEnviadas() {
            return notificacionesTransferencia;
        }
    }

    static class DummyComprobanteService implements ComprobanteService {
        @Override
        public void imprimir(Cuenta origen, Cuenta destino, double monto, double comision) {
            // No imprime nada en consola para mantener tests limpios
        }
    }

    static class DummyAuditoriaService implements AuditoriaService {
        @Override
        public void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
            // No imprime nada en consola para mantener tests limpios
        }
    }

    static class FakeAuditoriaService implements AuditoriaService {
        private int registros = 0;

        @Override
        public void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
            registros++;
        }

        public int getRegistros() {
            return registros;
        }
    }
}
