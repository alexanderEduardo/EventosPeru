package com.alex.spring.security.demo.test;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.repository.*;
import com.alex.spring.security.demo.services.MatrimonioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
@SpringBootTest
public class MatrimonioServiceTest {

    @Mock
    private LocalRepository localRepository;

    @Mock
    private LocalServicioRepository localServicioRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private SolicitudServicioRepository solicitudServicioRepository;

    @InjectMocks
    private MatrimonioServiceImpl matrimonioService;

    private Local local;
    private Cliente cliente;
    private Solicitud solicitud;
    private UserEntity userEntity;
    private Servicio servicio;

    @BeforeEach
    public void setUp() {

        local = Local.builder()
                .idLocal(1)
                .nombreLocal("Salón Real")
                .direccion("Av. Los Jardines 123, Lima")
                .capacidad(150)
                .descripcion("Un elegante salón con amplias instalaciones y servicios de alta calidad.")
                .precioBase(BigDecimal.valueOf(2500))
                .imagen("salon_real.webp")
                .build();

        userEntity = UserEntity.builder()
                .username("Alex")
                .email("alex@gmail.com")
                .build();

        cliente = new Cliente(1,userEntity, "Peña", "Av. Lima", "999299229");

        solicitud = new Solicitud(1, cliente, local,LocalDate.of(2024,12,25), 50,
                Solicitud.EstadoSolicitud.RECIBIDO, LocalDateTime.now(),LocalDateTime.of(2024, 5, 20,10,0,0),
                BigDecimal.valueOf(3000), "juan@correo.com");

        solicitud = Solicitud.builder()
                .idSolicitud(1)
                .cliente(cliente)
                .local(local)
                .fechaEvento(LocalDate.of(2024,12,25))
                .cantidadInvitados(50)
                .estado(Solicitud.EstadoSolicitud.RECIBIDO)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.of(2024, 5, 20,10,0,0))
                .presupuestoFinal(BigDecimal.valueOf(3000))
                .correo("alex-business@correo.com")
                .build();

        servicio = Servicio.builder()
                .idServicio(1)
                .nombre("Música en Vivo")
                .tipo("Música")
                .descripcion("Banda de jazz en vivo para eventos elegantes, incluye equipo de sonido.")
                .build();
    }

    @Test
    public void testGetLocales() {
        when(localRepository.findAll()).thenReturn(List.of(local));

        List<Local> locales = matrimonioService.getLocales();

        assertNotNull(locales);
        assertEquals(1, locales.size());
        assertEquals(local.getNombreLocal(), locales.get(0).getNombreLocal());

        verify(localRepository, times(1)).findAll();
    }

    @Test
    public void testFindLocalById() {
        when(localRepository.findById(1)).thenReturn(Optional.of(local));

        Optional<Local> resultado = matrimonioService.findLocalById(1);

        assertTrue(resultado.isPresent());
        assertEquals(local.getNombreLocal(), resultado.get().getNombreLocal());

        verify(localRepository, times(1)).findById(1);
    }

    @Test
    public void testCrearSolicitud() {
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);

        Solicitud solicitudCreada = matrimonioService.crearSolicitud(cliente, local,
                LocalDate.of(2024, 5, 20), 50,
                BigDecimal.valueOf(3000), "juan@correo.com");

        assertNotNull(solicitudCreada);
        assertEquals(cliente.getUsuario().getUsername(), solicitudCreada.getCliente().getUsuario().getUsername());
        assertEquals(local.getNombreLocal(), solicitudCreada.getLocal().getNombreLocal());
        assertEquals(Solicitud.EstadoSolicitud.RECIBIDO, solicitudCreada.getEstado());

        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    public void testGuardarServiciosParaSolicitud() {
        List<Integer> serviciosSeleccionados = List.of(1, 2);
        Servicio servicio3 = Servicio.builder()
                .idServicio(3)
                .nombre("Comida")
                .build();

        Servicio servicio4 = Servicio.builder()
                .idServicio(4)
                .nombre("Bailes")
                .build();

        LocalServicio localService1 = LocalServicio.builder()
                .idLocalServicio(1)
                .servicio(servicio3)
                .precio(BigDecimal.valueOf(500))
                .local(local)
                .build();

        LocalServicio localService2 =  LocalServicio.builder()
                .idLocalServicio(1)
                .servicio(servicio4)
                .precio(BigDecimal.valueOf(350))
                .local(local)
                .build();

        when(localServicioRepository.findById(1)).thenReturn(Optional.of(localService1));
        when(localServicioRepository.findById(2)).thenReturn(Optional.of(localService2));

        matrimonioService.guardarServiciosParaSolicitud(solicitud, serviciosSeleccionados);

        verify(solicitudServicioRepository, times(2)).save(any(SolicitudServicio.class));
    }

    @Test
    public void testFindSolicitudById() {
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(solicitud));

        Solicitud resultado = matrimonioService.findSolicitudById(1);

        assertNotNull(resultado);
        assertEquals(solicitud.getCliente().getUsuario().getUsername(),
                resultado.getCliente().getUsuario().getUsername());

        verify(solicitudRepository, times(1)).findById(1);
    }

    @Test
    public void testActualizarEstadoYObtenerSolicitudes() {
        when(solicitudRepository.findByClienteIdCliente(1)).thenReturn(List.of(solicitud));

        List<Solicitud> solicitudesActualizadas = matrimonioService.actualizarEstadoYObtenerSolicitudes(1);

        assertNotNull(solicitudesActualizadas);
        assertEquals(1, solicitudesActualizadas.size());
        assertEquals(Solicitud.EstadoSolicitud.RECIBIDO, solicitudesActualizadas.get(0).getEstado());

        verify(solicitudRepository, times(1)).updateEstadoSolicitudByClienteId(1);
        verify(solicitudRepository, times(1)).findByClienteIdCliente(1);
    }
}