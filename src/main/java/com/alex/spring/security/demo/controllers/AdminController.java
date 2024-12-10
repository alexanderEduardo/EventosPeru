package com.alex.spring.security.demo.controllers;

import com.alex.spring.security.demo.controllers.dto.*;
import com.alex.spring.security.demo.persistence.entity.*;
import com.alex.spring.security.demo.services.IUserService;
import com.alex.spring.security.demo.services.MatrimonioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SessionAttributes(names = {"cliente","proveedor","localDTO"})
@Controller
@RequestMapping("/admin")
public class AdminController {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    IUserService userService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MatrimonioService matrimonioService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String adminHome() {
        logger.info("Redirigiendo desde '/admin/' a '/home'.");
        return "redirect:/home";
    }


    //ver clientes
    @GetMapping("/clientes")
    public String clientes(Authentication authentication, Model model) {
        logAuthentication(authentication);
        logger.info("Accediendo al listado de clientes.");
        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", userService.findAllClients());
        return "admin/clientes";
    }

    @GetMapping("/form/clientes")
    public String formClientView(Authentication authentication, Model model) {
        logAuthentication(authentication);
        logger.info("Accediendo al formulario de creación de clientes.");
        Cliente cliente = Cliente.builder()
                .usuario(UserEntity.builder().build())
                .build();
        model.addAttribute("cliente", cliente);
        model.addAttribute("titulo", "Crear Cliente");
        return "admin/form-clientes";
    }

    @PostMapping("/form/clientes")
    public String formClientSave(@Valid Cliente cliente, BindingResult result, Authentication authentication, Model model, RedirectAttributes flash, SessionStatus status) {
        logAuthentication(authentication);
        if (result.hasErrors()) {
            logger.warn("Errores de validación en el formulario de cliente.");
            model.addAttribute("titulo", "Formulario de Cliente");
            return "admin/form-clientes";
        }

        try {
            if (cliente.getIdCliente() != null) {
                logger.info("Actualizando cliente con ID: " + cliente.getIdCliente());
                EditClientRequest clientRequest = new EditClientRequest(
                        cliente.getIdCliente(),
                        cliente.getUsuario().getUsername(),
                        cliente.getApellido(),
                        cliente.getUsuario().getEmail(),
                        cliente.getUsuario().getPassword(),
                        cliente.getDireccion(),
                        cliente.getTelefono(),
                        cliente.getUsuario().getIsEnabled(),
                        true, true, true);
                restTemplate.put("http://localhost:8080/api/clientes/update", clientRequest);
                flash.addFlashAttribute("success", "Cliente editado con éxito!");
            } else {
                logger.info("Creando nuevo cliente.");
                AuthRegisterRequest authRegisterRequest = new AuthRegisterRequest(
                        cliente.getUsuario().getUsername(),
                        cliente.getApellido(),
                        cliente.getUsuario().getEmail(),
                        cliente.getUsuario().getPassword(),
                        cliente.getDireccion(),
                        cliente.getTelefono(),
                        cliente.getUsuario().getIsEnabled(),
                        true, true, true);
                ResponseEntity<AuthRegisterResponse> response = restTemplate.postForEntity("http://localhost:8080/api/clientes/create",
                        authRegisterRequest, AuthRegisterResponse.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("Cliente creado exitosamente.");
                    flash.addFlashAttribute("success", "Cliente registrado con éxito en el sistema.");
                } else {
                    logger.error("Error al registrar el cliente: " + response.getStatusCode());
                    flash.addFlashAttribute("error", "Error en el registro del cliente en el sistema.");
                    return "redirect:/admin/clientes";
                }
            }
        } catch (Exception e) {
            logger.error("Error al conectar con el servicio de cliente.", e);
            flash.addFlashAttribute("error", "Error al conectar con el servicio de registro: " + e.getMessage());
            return "redirect:/admin/clientes";
        }

        status.setComplete();
        return "redirect:/admin/clientes";
    }

    @RequestMapping(value = "/form/clientes/{id}")
    public String editar(@PathVariable(value = "id") Integer id, Map<String, Object> model, RedirectAttributes flash) {
        logger.info("Intentando editar cliente con ID: " + id);
        try {
            if (id > 0) {
                ResponseEntity<GetClientResponse> response = restTemplate.getForEntity("http://localhost:8080/api/clientes/" + id, GetClientResponse.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("Cliente encontrado en el servicio.");
                    GetClientResponse responseBody = response.getBody();
                    UserEntity userEntity = UserEntity.builder()
                            .username(responseBody.getName())
                            .email(responseBody.getEmail())
                            .isEnabled(responseBody.getIsEnabled())
                            .accountNoLocked(responseBody.getIsAccountNoLocked())
                            .credentialNoExpired(responseBody.getIsCredentialNoExpired())
                            .accountNoExpired(responseBody.getIsAccountNoExpired())
                            .build();
                    Cliente cliente = Cliente.builder()
                            .idCliente(responseBody.getId())
                            .usuario(userEntity)
                            .apellido(responseBody.getLastname())
                            .telefono(responseBody.getPhone())
                            .direccion(responseBody.getAddress())
                            .build();
                    model.put("cliente", cliente);
                    model.put("titulo", "Editar Cliente");
                    return "admin/form-clientes";
                } else {
                    logger.warn("Cliente con ID " + id + " no encontrado.");
                    flash.addFlashAttribute("error", "El ID del cliente no existe en la BBDD!.");
                    return "redirect:/admin/clientes";
                }
            } else {
                logger.warn("ID de cliente inválido: " + id);
                flash.addFlashAttribute("error", "El ID del cliente no es válido!");
                return "redirect:/admin/clientes";
            }
        } catch (Exception e) {
            logger.error("Error al conectar con el servicio de búsqueda de cliente.", e);
            flash.addFlashAttribute("error", "Error al conectar con el servicio de búsqueda: " + e.getMessage());
            return "redirect:/admin/clientes";
        }
    }

    @RequestMapping(value = "clientes/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Integer id, RedirectAttributes flash) {
        logger.info("Intentando eliminar cliente con ID: " + id);

        try {
            if (id > 0) {
                logger.debug("Llamando al servicio REST para eliminar el cliente con ID: " + id);
                restTemplate.delete("http://localhost:8080/api/clientes/delete/" + id);
                logger.info("Cliente con ID " + id + " eliminado exitosamente.");
                flash.addFlashAttribute("success", "Cliente eliminado con éxito!");
            } else {
                logger.warn("ID inválido recibido para eliminación: " + id);
                flash.addFlashAttribute("error", "El ID no tiene formato válido");
            }
        } catch (Exception e) {
            logger.error("Error al intentar eliminar el cliente con ID: " + id, e);
            flash.addFlashAttribute("error", "Error al conectar con el servicio de eliminación: " + e.getMessage());
        }

        logger.info("Redirigiendo al listado de clientes.");
        return "redirect:/admin/clientes";
    }

    //ver proveedores
    @GetMapping("/proveedores")
    public String proveedores(Authentication authentication, Model model) {
        logger.info("Accediendo al listado de proveedores.");

        // Log del estado de autenticación
        if (authentication != null) {
            logger.info("Usuario autenticado: " + authentication.getName());
        } else {
            logger.warn("No hay usuario autenticado (authentication es null).");
        }

        try {
            logger.debug("Recuperando la lista de proveedores desde el servicio.");
            model.addAttribute("titulo", "Listado de proveedores");
            model.addAttribute("proveedores", userService.findAllProviders());
            logger.info("Lista de proveedores cargada exitosamente.");
        } catch (Exception e) {
            logger.error("Error al cargar la lista de proveedores.", e);
            model.addAttribute("error", "No se pudo cargar la lista de proveedores. Intente más tarde.");
        }

        return "admin/proveedores";
    }


    @GetMapping("/form/proveedores")
    public String formProviderView(Authentication authentication, Model model) {
        logger.info("Accediendo al formulario de creación de proveedores.");

        // Log del estado de autenticación
        if (authentication != null) {
            logger.info("Usuario autenticado: " + authentication.getName());
        } else {
            logger.warn("No hay usuario autenticado (authentication es null).");
        }

        try {
            logger.debug("Preparando el modelo para el formulario de creación de proveedores.");
            Proveedor proveedor = Proveedor.builder()
                    .usuario(UserEntity.builder().build())
                    .build();

            logger.debug("Recuperando la lista de especialidades.");
            List<Especialidad> especialidades = userService.getSpecialties();

            model.addAttribute("proveedor", proveedor);
            model.addAttribute("especialidades", especialidades);
            model.addAttribute("titulo", "Crear Proveedor");
            logger.info("Modelo preparado exitosamente para el formulario de creación de proveedores.");
        } catch (Exception e) {
            logger.error("Error al preparar el formulario de creación de proveedores.", e);
            model.addAttribute("error", "No se pudo cargar el formulario de creación de proveedores. Intente nuevamente.");
        }

        return "admin/form-proveedores";
    }


    @PostMapping("/form/proveedores")
    public String formProviderSave(@Valid Proveedor proveedor, BindingResult result, Model model, RedirectAttributes flash, SessionStatus status) {
        logger.info("Procesando formulario para guardar proveedor.");

        // Verificar errores de validación
        if (result.hasErrors()) {
            logger.warn("Errores de validación detectados en el formulario de proveedor.");
            model.addAttribute("titulo", "Formulario de Proveedor");
            List<Especialidad> especialidades = userService.getSpecialties();
            model.addAttribute("especialidades", especialidades);
            return "admin/form-proveedores";
        }

        try {
            if (proveedor.getIdProveedor() != null) {
                // Actualización de proveedor existente
                logger.info("Actualizando proveedor con ID: " + proveedor.getIdProveedor());
                EditProviderRequest providerRequest = new EditProviderRequest(
                        proveedor.getIdProveedor(),
                        proveedor.getUsuario().getUsername(),
                        proveedor.getUsuario().getEmail(),
                        proveedor.getUsuario().getPassword(),
                        proveedor.getEspecialidad(),
                        proveedor.getCalificacion(),
                        proveedor.getUsuario().getIsEnabled(),
                        true, true, true);

                logger.debug("Enviando solicitud PUT al servicio REST para actualizar proveedor.");
                restTemplate.put("http://localhost:8080/api/proveedores/update", providerRequest);
                logger.info("Proveedor con ID " + proveedor.getIdProveedor() + " actualizado exitosamente.");
                flash.addFlashAttribute("success", "Proveedor editado con éxito!");
            } else {
                // Creación de nuevo proveedor
                logger.info("Registrando nuevo proveedor.");
                CreateProviderRequest createProviderRequest = new CreateProviderRequest(
                        proveedor.getUsuario().getUsername(),
                        proveedor.getUsuario().getEmail(),
                        proveedor.getUsuario().getPassword(),
                        proveedor.getEspecialidad(),
                        proveedor.getCalificacion(),
                        proveedor.getUsuario().getIsEnabled(),
                        true, true, true);

                logger.debug("Enviando solicitud POST al servicio REST para crear proveedor.");
                ResponseEntity<AuthRegisterResponse> response = restTemplate.postForEntity("http://localhost:8080/api/proveedores/create",
                        createProviderRequest, AuthRegisterResponse.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("Proveedor registrado exitosamente.");
                    flash.addFlashAttribute("success", "Proveedor registrado con éxito en el sistema.");
                } else {
                    logger.error("Error al registrar proveedor. Código de respuesta: " + response.getStatusCode());
                    flash.addFlashAttribute("error", "Error en el registro del proveedor en el sistema.");
                    return "redirect:/admin/proveedores";
                }
            }
        } catch (Exception e) {
            logger.error("Error al conectar con el servicio de registro de proveedores.", e);
            flash.addFlashAttribute("error", "Error al conectar con el servicio de registro: " + e.getMessage());
            return "redirect:/admin/proveedores";
        }

        status.setComplete();
        logger.info("Finalizado procesamiento del formulario de proveedores.");
        return "redirect:/admin/proveedores";
    }

    @RequestMapping(value = "/form/proveedores/{id}")
    public String editarProveedor(@PathVariable(value = "id") Integer id, Map<String, Object> model, RedirectAttributes flash) {
        logger.info("Accediendo al formulario de edición para el proveedor con ID: " + id);

        Proveedor proveedor = null;
        try {
            if (id > 0) {
                logger.debug("Enviando solicitud GET al servicio REST para obtener los datos del proveedor con ID: " + id);
                ResponseEntity<GetProviderResponse> response = restTemplate.getForEntity("http://localhost:8080/api/proveedores/" + id, GetProviderResponse.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("Proveedor con ID " + id + " encontrado exitosamente en el servicio.");

                    // Construir objetos para la vista
                    GetProviderResponse responseBody = response.getBody();
                    UserEntity userEntity = UserEntity.builder()
                            .username(responseBody.getName())
                            .email(responseBody.getEmail())
                            .isEnabled(responseBody.getIsEnabled())
                            .accountNoLocked(responseBody.getIsAccountNoLocked())
                            .credentialNoExpired(responseBody.getIsCredentialNoExpired())
                            .accountNoExpired(responseBody.getIsAccountNoExpired())
                            .build();

                    proveedor = Proveedor.builder()
                            .idProveedor(responseBody.getId())
                            .usuario(userEntity)
                            .especialidad(responseBody.getEspecialidad())
                            .calificacion(responseBody.getCalificacion())
                            .build();

                    logger.debug("Preparando el modelo para la vista del formulario de edición.");
                    model.put("proveedor", proveedor);
                    model.put("titulo", "Editar Proveedor");
                    List<Especialidad> especialidades = userService.getSpecialties();
                    model.put("especialidades", especialidades);

                    logger.info("Modelo preparado exitosamente para la vista del formulario de edición.");
                    return "admin/form-proveedores";

                } else {
                    logger.warn("Proveedor con ID " + id + " no encontrado en el servicio. Código de respuesta: " + response.getStatusCode());
                    flash.addFlashAttribute("error", "El ID del proveedor no existe en la BBDD!.");
                    return "redirect:/admin/proveedores";
                }
            } else {
                logger.warn("ID inválido recibido: " + id);
                flash.addFlashAttribute("error", "El ID del proveedor no es válido!");
                return "redirect:/admin/proveedores";
            }
        } catch (Exception e) {
            logger.error("Error al conectar con el servicio de búsqueda del proveedor con ID: " + id, e);
            flash.addFlashAttribute("error", "Error al conectar con el servicio de búsqueda: " + e.getMessage());
            return "redirect:/admin/proveedores";
        }
    }

    @RequestMapping(value = "proveedores/eliminar/{id}")
    public String eliminarProveedor(@PathVariable(value = "id") Integer id, RedirectAttributes flash) {
        logger.info("Intentando eliminar proveedor con ID: " + id);

        try {
            if (id > 0) {
                logger.debug("Llamando al servicio REST para eliminar el proveedor con ID: " + id);
                restTemplate.delete("http://localhost:8080/api/proveedores/delete/" + id);
                logger.info("Proveedor con ID " + id + " eliminado exitosamente.");
                flash.addFlashAttribute("success", "Proveedor eliminado con éxito!");
            } else {
                logger.warn("ID inválido recibido para eliminación: " + id);
                flash.addFlashAttribute("error", "El ID no tiene formato válido");
            }
        } catch (Exception e) {
            logger.error("Error al intentar eliminar el proveedor con ID: " + id, e);
            flash.addFlashAttribute("error", "Error al conectar con el servicio de eliminación: " + e.getMessage());
        }

        logger.info("Redirigiendo al listado de proveedores.");
        return "redirect:/admin/proveedores";
    }

    @RequestMapping(value = "/solicitudes")
    public String solicitudesEventos(Model model) {
        logger.info("Accediendo al listado de solicitudes de eventos.");

        try {
            logger.debug("Recuperando la lista de solicitudes desde el servicio.");
            List<Solicitud> solicitudes = matrimonioService.obtenerSolicitudes();
            model.addAttribute("solicitudes", solicitudes);
            logger.info("Lista de solicitudes cargada exitosamente. Total de solicitudes: " + solicitudes.size());
        } catch (Exception e) {
            logger.error("Error al cargar la lista de solicitudes de eventos.", e);
            model.addAttribute("error", "No se pudo cargar la lista de solicitudes. Intente nuevamente.");
        }

        return "admin/solicitudes";
    }

    @RequestMapping(value = "/exportar-solicitudes", method = RequestMethod.GET)
    public void exportarSolicitudes(HttpServletResponse response) throws IOException {
        logger.info("Iniciando exportación de solicitudes a archivo Excel.");

        try {
            // Obtener todas las solicitudes
            logger.debug("Recuperando la lista de solicitudes desde el servicio.");
            List<Solicitud> solicitudes = matrimonioService.obtenerSolicitudes();
            logger.info("Lista de solicitudes obtenida exitosamente. Total de solicitudes: " + solicitudes.size());

            // Configura la respuesta HTTP
            logger.debug("Configurando la respuesta HTTP para el archivo Excel.");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=solicitudes.xlsx");

            // Crear el archivo Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Solicitudes");

            // Crear la fila de encabezado
            logger.debug("Creando fila de encabezado para el archivo Excel.");
            Row headerRow = sheet.createRow(0);
            String[] columnas = {"ID", "Local", "Cliente", "Correo", "Fecha Evento", "Cantidad Invitados",
                    "Presupuesto Final", "Fecha de Actualización", "Fecha Creación", "Estado"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
            }

            // Llenar los datos
            logger.debug("Llenando el archivo Excel con los datos de las solicitudes.");
            int rowNum = 1;
            for (Solicitud solicitud : solicitudes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(solicitud.getIdSolicitud());
                row.createCell(1).setCellValue(solicitud.getLocal() != null ? solicitud.getLocal().getNombreLocal() : "");
                row.createCell(2).setCellValue(solicitud.getCliente() != null ? solicitud.getCliente().getUsuario().getUsername() + " " + solicitud.getCliente().getApellido() : "");
                row.createCell(3).setCellValue(solicitud.getCorreo());
                row.createCell(4).setCellValue(solicitud.getFechaEvento().toString());
                row.createCell(5).setCellValue(solicitud.getCantidadInvitados());
                row.createCell(6).setCellValue(solicitud.getPresupuestoFinal() != null ? solicitud.getPresupuestoFinal().toString() : "");
                row.createCell(7).setCellValue(solicitud.getFechaActualizacion().toString());
                row.createCell(8).setCellValue(solicitud.getFechaCreacion().toString());
                row.createCell(9).setCellValue(solicitud.getEstado().toString());
            }

            // Ajustar columnas al contenido
            logger.debug("Ajustando columnas del archivo Excel al contenido.");
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo en la respuesta
            logger.debug("Escribiendo el archivo Excel en la respuesta HTTP.");
            workbook.write(response.getOutputStream());
            workbook.close();
            logger.info("Exportación de solicitudes completada exitosamente.");
        } catch (IOException e) {
            logger.error("Error al generar el archivo Excel para las solicitudes.", e);
            throw e;
        }
    }

    @RequestMapping(value = "/exportar-eventos-por-mes", method = RequestMethod.GET)
    public void exportarEventosPorMes(HttpServletResponse response) throws IOException {
        logger.info("Iniciando exportación de eventos por mes a archivo Excel.");

        try {
            // Obtener todas las solicitudes
            logger.debug("Recuperando la lista de solicitudes desde el servicio.");
            List<Solicitud> solicitudes = matrimonioService.obtenerSolicitudes();
            logger.info("Lista de solicitudes obtenida exitosamente. Total de solicitudes: " + solicitudes.size());

            // Agrupar eventos por mes
            logger.debug("Agrupando solicitudes por mes y contando eventos.");
            Map<Month, Long> eventosPorMes = solicitudes.stream()
                    .collect(Collectors.groupingBy(
                            solicitud -> solicitud.getFechaEvento().getMonth(), // Agrupar por mes
                            Collectors.counting() // Contar eventos por mes
                    ));
            logger.info("Agrupación por mes completada exitosamente. Total de meses con eventos: " + eventosPorMes.size());

            // Configurar la respuesta HTTP
            logger.debug("Configurando la respuesta HTTP para el archivo Excel.");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=eventos_por_mes.xlsx");

            // Crear el archivo Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Eventos por Mes");

            // Crear la fila de encabezado
            logger.debug("Creando fila de encabezado para el archivo Excel.");
            Row headerRow = sheet.createRow(0);
            String[] columnas = {"Mes", "Cantidad de Eventos"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
            }

            // Llenar los datos
            logger.debug("Llenando el archivo Excel con los datos agrupados por mes.");
            int rowNum = 1;
            for (Map.Entry<Month, Long> entry : eventosPorMes.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey().toString()); // Nombre del mes
                row.createCell(1).setCellValue(entry.getValue()); // Cantidad de eventos
            }

            // Ajustar las columnas al contenido
            logger.debug("Ajustando columnas del archivo Excel al contenido.");
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo en la respuesta
            logger.debug("Escribiendo el archivo Excel en la respuesta HTTP.");
            workbook.write(response.getOutputStream());
            workbook.close();
            logger.info("Exportación de eventos por mes completada exitosamente.");
        } catch (IOException e) {
            logger.error("Error al generar el archivo Excel para los eventos por mes.", e);
            throw e;
        }
    }


    @GetMapping("/locales")
    public String locales(Authentication authentication, Model model) {
        logger.info("Accediendo al listado de locales.");

        // Log de autenticación
        if (authentication != null) {
            logger.info("Usuario autenticado: " + authentication.getName());
        } else {
            logger.warn("No hay usuario autenticado (authentication es null).");
        }

        try {
            logger.debug("Recuperando la lista de locales desde el servicio.");
            model.addAttribute("titulo", "Listado de locales");
            model.addAttribute("locales", matrimonioService.getLocales());
            logger.info("Lista de locales cargada exitosamente.");
        } catch (Exception e) {
            logger.error("Error al cargar la lista de locales.", e);
            model.addAttribute("error", "No se pudo cargar la lista de locales. Intente más tarde.");
        }

        return "admin/locales";
    }

    @GetMapping("/form/locales")
    public String formLocalesView(Model model) {
        logger.info("Accediendo al formulario de creación de locales.");

        try {
            logger.debug("Preparando el modelo para el formulario de creación de locales.");
            LocalDTO localDTO = LocalDTO.builder().build();
            model.addAttribute("localDTO", localDTO);
            model.addAttribute("titulo", "Crear Local");
            logger.info("Modelo preparado exitosamente para el formulario de creación de locales.");
        } catch (Exception e) {
            logger.error("Error al preparar el formulario de creación de locales.", e);
            model.addAttribute("error", "No se pudo cargar el formulario de creación de locales. Intente nuevamente.");
        }

        return "admin/form-local";
    }

    @PostMapping("/form/locales")
    public String saveLocal(@Validated @ModelAttribute("localDTO") LocalDTO localDTO,
                            BindingResult result,
                            Model model,
                            RedirectAttributes flash, SessionStatus status) throws IOException {
        logger.info("Procesando formulario para guardar local.");

        // Validación de errores
        if (localDTO.getImagen().getSize() == 0) {
            logger.warn("No se cargó una imagen en el formulario.");
            result.rejectValue("imagen", null, "Debes cargar una imagen");
        }
        if (result.hasErrors()) {
            logger.warn("Errores de validación detectados en el formulario de local.");
            model.addAttribute("titulo", "Crear Local");
            return "admin/form-local";
        }

        try {
            // Creación del objeto Local
            logger.debug("Creando el objeto Local a partir del formulario.");
            Local local = new Local();
            local.setIdLocal(localDTO.getIdLocal());
            local.setNombreLocal(localDTO.getNombreLocal());
            local.setDireccion(localDTO.getDireccion());
            local.setDescripcion(localDTO.getDescripcion());
            local.setCapacidad(localDTO.getCapacidad());
            local.setPrecioBase(localDTO.getPrecioBase());

            // Procesamiento de la imagen
            MultipartFile image = localDTO.getImagen();
            if (image != null && !image.isEmpty()) {
                logger.debug("Procesando la imagen cargada en el formulario.");
                String imagenNombre = image.getOriginalFilename();
                File saveFile = new ClassPathResource("static/images/upload").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "locales" + File.separator + image.getOriginalFilename());
                Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                local.setImagen(imagenNombre);
            }

            // Guardar en la base de datos
            logger.debug("Guardando el local en la base de datos.");
            matrimonioService.saveLocal(local);
            logger.info("Local guardado exitosamente con ID: " + local.getIdLocal());
            flash.addFlashAttribute("success", "Local guardado con éxito.");
        } catch (IOException e) {
            logger.error("Error al procesar la imagen del local.", e);
            flash.addFlashAttribute("error", "Ocurrió un error al guardar el local. Intente nuevamente.");
            return "admin/form-local";
        } catch (Exception e) {
            logger.error("Error al guardar el local en la base de datos.", e);
            flash.addFlashAttribute("error", "Ocurrió un error inesperado al guardar el local. Intente nuevamente.");
            return "admin/form-local";
        }

        status.setComplete();
        return "redirect:/admin/locales";
    }

    @RequestMapping(value = "/form/locales/{id}")
    public String editarLocal(@PathVariable(value = "id") Integer id, Map<String, Object> model, RedirectAttributes flash) {
        logger.info("Accediendo al formulario de edición para el local con ID: " + id);

        try {
            Optional<Local> localOptional = matrimonioService.findLocalById(id);

            if (localOptional.isPresent()) {
                logger.info("Local con ID " + id + " encontrado.");
                Local local = localOptional.get();

                LocalDTO localDTO = LocalDTO.builder()
                        .nombreLocal(local.getNombreLocal())
                        .idLocal(local.getIdLocal())
                        .capacidad(local.getCapacidad())
                        .descripcion(local.getDescripcion())
                        .precioBase(local.getPrecioBase())
                        .direccion(local.getDireccion())
                        .build();

                model.put("localDTO", localDTO);
                model.put("titulo", "Editar Local");
                logger.debug("Modelo preparado exitosamente para la edición del local.");
            } else {
                logger.warn("No se encontró un local con el ID: " + id);
                flash.addFlashAttribute("error", "El local con el ID especificado no existe.");
                return "redirect:/admin/locales";
            }
        } catch (Exception e) {
            logger.error("Error al buscar el local con ID: " + id, e);
            flash.addFlashAttribute("error", "Ocurrió un error al intentar cargar los datos del local.");
            return "redirect:/admin/locales";
        }

        return "admin/form-local";
    }

    @RequestMapping("locales/eliminar/{id}")
    public String eliminarLocal(@PathVariable Integer id, RedirectAttributes flash) {
        logger.info("Intentando eliminar local con ID: " + id);

        try {
            if (id > 0) {
                logger.debug("Llamando al servicio para eliminar el local con ID: " + id);
                matrimonioService.deleteLocalById(id);
                logger.info("Local con ID " + id + " eliminado exitosamente.");
                flash.addFlashAttribute("success", "Local eliminado con éxito!");
            } else {
                logger.warn("ID inválido recibido: " + id);
                flash.addFlashAttribute("error", "El ID no tiene formato válido.");
            }
        } catch (Exception e) {
            logger.error("Error al intentar eliminar el local con ID: " + id, e);
            flash.addFlashAttribute("error", "Ocurrió un error inesperado al tratar de eliminar el local.");
        }

        return "redirect:/admin/locales";
    }

    private void logAuthentication(Authentication authentication) {
        if (authentication != null) {
            logger.info("Usuario autenticado: " + authentication.getName());
        } else {
            logger.warn("No hay usuario autenticado.");
        }
    }


}
