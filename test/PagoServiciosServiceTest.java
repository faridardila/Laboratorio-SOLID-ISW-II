import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PagoServiciosServiceTest {

    private TransaccionServiceTest.FakeTransaccionRepositorio fakeRepositorio;
    private SpyComprobanteService spyComprobante;
    private PagoServiciosService pagoServicios;

    private CuentaOperativa cuenta;
    private FacturaServicioPublico factura;

    @BeforeEach
    void setUp() {
        fakeRepositorio = new TransaccionServiceTest.FakeTransaccionRepositorio();
        spyComprobante = new SpyComprobanteService();

        TransaccionService transaccionService = new TransaccionService(
                new ValidadorMontoTope(),
                fakeRepositorio,
                spyComprobante,
                new TransaccionServiceTest.FakeNotificacionService(),
                new TransaccionServiceTest.DummyAuditoriaService()
        );
        pagoServicios = new PagoServiciosService(transaccionService);

        cuenta = new CuentaAhorros("001-1", "Ana", 1_000_000);
        factura = new FacturaServicioPublico("FAC-LUZ-778812", TipoServicioPublico.LUZ);
    }

    @Test
    void testPagoDescuentaValorMasComisionGuardaEImprimeReferencia() {
        // Criterio de aceptación R6: un pago de $184.300 descuenta $185.800 de la cuenta,
        // guarda la transacción e imprime el comprobante con la referencia como destino.
        pagoServicios.pagar(cuenta, factura, 184_300);

        assertEquals(1_000_000 - 185_800, cuenta.getSaldo(), 0.001);

        assertEquals(1, fakeRepositorio.getTransaccionesGuardadas());
        TransaccionServiceTest.RegistroTransaccion registro = fakeRepositorio.getTransacciones().get(0);
        assertEquals("001-1", registro.origen);
        assertEquals("FAC-LUZ-778812", registro.destino);
        assertEquals(184_300, registro.monto, 0.001);
        assertEquals(1_500, registro.comision, 0.001);

        assertEquals(1, spyComprobante.impresiones);
        assertEquals("FAC-LUZ-778812", spyComprobante.destinoImpreso);
    }

    static class SpyComprobanteService implements ComprobanteService {
        int impresiones = 0;
        String destinoImpreso;

        @Override
        public void imprimir(Cuenta origen, Cuenta destino, double monto, double comision) {
            impresiones++;
            destinoImpreso = destino.getNumero();
        }
    }
}
