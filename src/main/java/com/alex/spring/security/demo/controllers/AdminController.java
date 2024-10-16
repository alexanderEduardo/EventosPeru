package com.alex.spring.security.demo.controllers;

import com.alex.spring.security.demo.controllers.dto.*;
import com.alex.spring.security.demo.persistence.entity.Cliente;
import com.alex.spring.security.demo.persistence.entity.Especialidad;
import com.alex.spring.security.demo.persistence.entity.Proveedor;
import com.alex.spring.security.demo.persistence.entity.UserEntity;
import com.alex.spring.security.demo.services.IUserService;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SessionAttributes(names = {"cliente","proveedor"})
@Controller
@RequestMapping("/admin")
public class AdminController {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    IUserService userService;

    @Autowired
    RestTemplate restTemplate;

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

}
