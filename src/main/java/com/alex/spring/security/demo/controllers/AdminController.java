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
import org.springframework.security.core.userdetails.User;
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
import java.time.LocalDate;
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

    @RequestMapping(value =  "/", method = RequestMethod.GET)
    public String adminHome(){
        return "redirect:/home";
    }


    //ver clientes
    @GetMapping("/clientes")
    public String clientes(Authentication authentication, Model model) {
        if (authentication != null) {
            logger.info("Hola usuario autenticado, tu username es: ".concat(authentication.getName()));
        } else {
            logger.info("authentication es null");
        }
        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", userService.findAllClients());

        return "admin/clientes";
    }

    @GetMapping("/form/clientes")
    public String formClientView(Authentication authentication, Model model) {
        Cliente cliente = Cliente.builder()
                .usuario(UserEntity.builder().build())
                .build();

        model.addAttribute("cliente", cliente);
        model.addAttribute("titulo", "Crear Cliente");
        return "admin/form-clientes";
    }

    @PostMapping("/form/clientes")
    public String formClientSave(@Valid Cliente cliente, BindingResult result, Authentication authentication, Model model, RedirectAttributes flash, SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Formulario de Cliente");
            return "admin/form-clientes";
        }

        try {

            if (cliente.getIdCliente() != null) {
                //update
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
                //insert
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

                // Verificar si el registro fue exitoso
                if (response.getStatusCode() == HttpStatus.OK) {
                    flash.addFlashAttribute("success", "Cliente registrado con éxito en el sistema.");
                } else {
                    flash.addFlashAttribute("error", "Error en el registro del cliente en el sistema.");
                    return "redirect:/admin/clientes";
                }
            }

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al conectar con el servicio de registro: " + e.getMessage());
            return "redirect:/admin/clientes";
        }

        status.setComplete();
        return "redirect:/admin/clientes";
    }

    @RequestMapping(value = "/form/clientes/{id}")
    public String editar(@PathVariable(value = "id") Integer id, Map<String, Object> model, RedirectAttributes flash) {

        Cliente cliente = null;
        try {
            if (id > 0) {

                ResponseEntity<GetClientResponse> response = restTemplate.getForEntity("http://localhost:8080/api/clientes/".concat(String.valueOf(id)), GetClientResponse.class);
                //cliente = userService.findClient(id);\
                if (response.getStatusCode() == HttpStatus.OK) {
                    GetClientResponse responseBody = response.getBody();
                    UserEntity userEntity = UserEntity.builder()
                            .username(responseBody.getName())
                            .email(responseBody.getEmail())
                            .isEnabled(responseBody.getIsEnabled())
                            .accountNoLocked(responseBody.getIsAccountNoLocked())
                            .credentialNoExpired(responseBody.getIsCredentialNoExpired())
                            .accountNoExpired(responseBody.getIsAccountNoExpired())
                            .build();

                    cliente = Cliente.builder()
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
                    flash.addFlashAttribute("error", "El ID del cliente no existe en la BBDD!.");
                    return "redirect:/admin/clientes";
                }

            } else {
                flash.addFlashAttribute("error", "El ID del cliente no es valido!");
                return "redirect:/admin/clientes";
            }

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al conectar con el servicio de busqueda: " + e.getMessage());
            return "redirect:/admin/clientes";
        }

    }

    @RequestMapping(value = "clientes/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Integer id, RedirectAttributes flash) {
        try {
            if (id > 0) {
                restTemplate.delete("http://localhost:8080/api/clientes/delete/".concat(String.valueOf(id)));
                flash.addFlashAttribute("success", "Cliente eliminado con éxito!");
                return "redirect:/admin/clientes";
            } else {
                flash.addFlashAttribute("error", "El id no tiene formato valido");
                return "redirect:/admin/clientes";
            }

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al conectar con el servicio de eliminacion: " + e.getMessage());
            return "redirect:/admin/clientes";
        }
    }


    //ver proveedores
    @GetMapping("/proveedores")
    public String proveedores(Authentication authentication, Model model) {
        if (authentication != null) {
            logger.info("Hola usuario autenticado, tu username es: ".concat(authentication.getName()));
        } else {
            logger.info("authentication es null");
        }
        model.addAttribute("titulo", "Listado de proveedores");
        model.addAttribute("proveedores", userService.findAllProviders());

        return "admin/proveedores";
    }

    @GetMapping("/form/proveedores")
    public String formProviderView(Authentication authentication, Model model) {
        Proveedor proveedor = Proveedor.builder()
                .usuario(UserEntity.builder().build())
                .build();
        List<Especialidad> especialidades = userService.getSpecialties();

        model.addAttribute("proveedor", proveedor);
        model.addAttribute("especialidades", especialidades);
        model.addAttribute("titulo", "Crear Proveedor");
        return "admin/form-proveedores";
    }

    @PostMapping("/form/proveedores")
    public String formProviderSave(@Valid Proveedor proveedor, BindingResult result, Authentication authentication, Model model, RedirectAttributes flash, SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Formulario de Proveedor");
            List<Especialidad> especialidades = userService.getSpecialties();
            model.addAttribute("especialidades", especialidades);
            return "admin/form-proveedores";
        }

        try {

            if (proveedor.getIdProveedor() != null) {
                //update
                EditProviderRequest providerRequest = new EditProviderRequest(
                        proveedor.getIdProveedor(),
                        proveedor.getUsuario().getUsername(),
                        proveedor.getUsuario().getEmail(),
                        proveedor.getUsuario().getPassword(),
                        proveedor.getEspecialidad(),
                        proveedor.getCalificacion(),
                        proveedor.getUsuario().getIsEnabled(),
                        true, true, true);

                restTemplate.put("http://localhost:8080/api/proveedores/update", providerRequest);
                flash.addFlashAttribute("success", "Proveedor editado con éxito!");
            } else {
                //insert
                CreateProviderRequest createProviderRequest = new CreateProviderRequest(
                        proveedor.getUsuario().getUsername(),
                        proveedor.getUsuario().getEmail(),
                        proveedor.getUsuario().getPassword(),
                        proveedor.getEspecialidad(),
                        proveedor.getCalificacion(),
                        proveedor.getUsuario().getIsEnabled(),
                        true, true, true);

                ResponseEntity<AuthRegisterResponse> response = restTemplate.postForEntity("http://localhost:8080/api/proveedores/create",
                        createProviderRequest, AuthRegisterResponse.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    flash.addFlashAttribute("success", "Proveedor registrado con éxito en el sistema.");
                } else {
                    flash.addFlashAttribute("error", "Error en el registro del proveedor en el sistema.");
                    return "redirect:/admin/proveedores";
                }
            }

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al conectar con el servicio de registro: " + e.getMessage());
            return "redirect:/admin/proveedores";
        }

        status.setComplete();
        return "redirect:/admin/proveedores";
    }

    @RequestMapping(value = "/form/proveedores/{id}")
    public String editarProveedor(@PathVariable(value = "id") Integer id, Map<String, Object> model, RedirectAttributes flash) {

        Proveedor proveedor = null;
        try {
            if (id > 0) {

                ResponseEntity<GetProviderResponse> response = restTemplate.getForEntity("http://localhost:8080/api/proveedores/".concat(String.valueOf(id)), GetProviderResponse.class);
                if (response.getStatusCode() == HttpStatus.OK) {
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

                    model.put("proveedor", proveedor);
                    model.put("titulo", "Editar Proveedor");
                    List<Especialidad> especialidades = userService.getSpecialties();
                    model.put("especialidades", especialidades);
                    return "admin/form-proveedores";

                } else {
                    flash.addFlashAttribute("error", "El ID del proveedor no existe en la BBDD!.");
                    return "redirect:/admin/proveedores";
                }

            } else {
                flash.addFlashAttribute("error", "El ID del proveedor no es valido!");
                return "redirect:/admin/proveedores";
            }

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al conectar con el servicio de busqueda: " + e.getMessage());
            return "redirect:/admin/proveedores";
        }

    }

    @RequestMapping(value = "proveedores/eliminar/{id}")
    public String eliminarProveedor(@PathVariable(value = "id") Integer id, RedirectAttributes flash) {
        try {
            if (id > 0) {
                restTemplate.delete("http://localhost:8080/api/proveedores/delete/".concat(String.valueOf(id)));
                flash.addFlashAttribute("success", "Proveedor eliminado con éxito!");
                return "redirect:/admin/proveedores";
            } else {
                flash.addFlashAttribute("error", "El id no tiene formato valido");
                return "redirect:/admin/proveedores";
            }

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al conectar con el servicio de eliminacion: " + e.getMessage());
            return "redirect:/admin/proveedores";
        }
    }

    @RequestMapping(value = "/solicitudes")
    public String solicitudesEventos(Model model){
        List<Solicitud> solicitudes = matrimonioService.obtenerSolicitudes();
        model.addAttribute("solicitudes",solicitudes);
        return "admin/solicitudes";
    }

    @RequestMapping(value = "/exportar-solicitudes", method = RequestMethod.GET)
    public void exportarSolicitudes(HttpServletResponse response) throws IOException {
        // Obtener todas las solicitudes
        List<Solicitud> solicitudes = matrimonioService.obtenerSolicitudes();

        // Configura la respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=solicitudes.xlsx");

        // Crear el archivo Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Solicitudes");

        // Crear la fila de encabezado
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"ID", "Local", "Cliente", "Correo", "Fecha Evento", "Cantidad Invitados",
                "Presupuesto Final", "Fecha de Actualización", "Fecha Creación", "Estado"};
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
        }

        // Llenar los datos
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
        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Escribir el archivo en la respuesta
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @RequestMapping(value = "/exportar-eventos-por-mes", method = RequestMethod.GET)
    public void exportarEventosPorMes(HttpServletResponse response) throws IOException {
        // Obtener todas las solicitudes
        List<Solicitud> solicitudes = matrimonioService.obtenerSolicitudes();

        Map<Month, Long> eventosPorMes = solicitudes.stream()
                .collect(Collectors.groupingBy(
                        solicitud -> solicitud.getFechaEvento().getMonth(), // Agrupar por mes
                        Collectors.counting() // Contar eventos por mes
                ));

        // Configurar la respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=eventos_por_mes.xlsx");

        // Crear el archivo Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Eventos por Mes");

        // Crear la fila de encabezado
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"Mes", "Cantidad de Eventos"};
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
        }

        // Llenar los datos
        int rowNum = 1;
        for (Map.Entry<Month, Long> entry : eventosPorMes.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().toString()); // Nombre del mes
            row.createCell(1).setCellValue(entry.getValue()); // Cantidad de eventos
        }

        // Ajustar las columnas al contenido
        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Escribir el archivo en la respuesta
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    @GetMapping("/locales")
    public String locales(Authentication authentication, Model model) {
        if (authentication != null) {
            logger.info("Hola usuario autenticado, tu username es: ".concat(authentication.getName()));
        } else {
            logger.info("authentication es null");
        }
        model.addAttribute("titulo", "Listado de locales");
        model.addAttribute("locales", matrimonioService.getLocales());
        return "admin/locales";
    }
    @GetMapping("/form/locales")
    public String formLocalesView(Model model) {
        LocalDTO localDTO = LocalDTO.builder()
                .build();

        model.addAttribute("localDTO", localDTO);
        model.addAttribute("titulo", "Crear Local");
        return "admin/form-local";
    }
    @PostMapping("/form/locales")
    public String saveLocal(@Validated @ModelAttribute("localDTO") LocalDTO localDTO,
                            BindingResult result,
                            Model model,
                            RedirectAttributes flash,SessionStatus status) throws IOException {

        // Verificamos si hay errores de validación
        if(localDTO.getImagen().getSize() == 0){
            result.rejectValue("imagen",null,"Debes cargar una imagen");
        }
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Crear Local");
            return "admin/form-local";
        }

        // Creamos un nuevo objeto Local
        Local local = new Local();
        local.setIdLocal(localDTO.getIdLocal());
        local.setNombreLocal(localDTO.getNombreLocal());
        local.setDireccion(localDTO.getDireccion());
        local.setDescripcion(localDTO.getDescripcion());
        local.setCapacidad(localDTO.getCapacidad());
        local.setPrecioBase(localDTO.getPrecioBase());

        // Procesamos la imagen si está presente
        MultipartFile image = localDTO.getImagen();
        if (image != null && !image.isEmpty()) {
            String imagenNombre = image.getOriginalFilename();
            File saveFile = new ClassPathResource("static/images/upload").getFile();

            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "locales" + File.separator
                    + image.getOriginalFilename());

            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            local.setImagen(imagenNombre);
        }

        // Guardamos el local en la base de datos
        matrimonioService.saveLocal(local);
        status.setComplete();
        flash.addFlashAttribute("success", "Local guardado con éxito");
        return "redirect:/admin/locales";
    }

    @RequestMapping(value = "/form/locales/{id}")
    public String editarLocal(@PathVariable(value = "id") Integer id, Map<String, Object> model, RedirectAttributes flash) {
        Optional<Local> localOptional = matrimonioService.findLocalById(id);
        if(localOptional.isPresent()){
            Local local = localOptional.get();
            LocalDTO localDTO = LocalDTO.builder()
                    .nombreLocal(local.getNombreLocal())
                    .idLocal(local.getIdLocal())
                    .capacidad(local.getCapacidad())
                    .descripcion(local.getDescripcion())
                    .precioBase(local.getPrecioBase())
                    .direccion(local.getDireccion())
                    .build();

            model.put("localDTO",localDTO);
            model.put("titulo", "Editar Cliente");
        }
        return "admin/form-local";
    }

    @RequestMapping("locales/eliminar/{id}")
    public String eliminarLocal(@PathVariable Integer id, RedirectAttributes flash) {

        try {
            if (id > 0) {
                matrimonioService.deleteLocalById(id);
                flash.addFlashAttribute("success", "Local eliminado con éxito!");
            } else {
                flash.addFlashAttribute("error", "El id no tiene formato valido");
            }
            return "redirect:/admin/locales";

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Ocurrio un error inesperado al tratar de eliminar el local.");
            return "redirect:/admin/locales";
        }
    }

}
