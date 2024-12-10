package com.alex.spring.security.demo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {
    protected final Log logger = LogFactory.getLog(this.getClass());

    @RequestMapping(value = { "/", "/home" }, method = RequestMethod.GET)
    public String home(Model model, Authentication authentication, HttpServletRequest request) {
        try {
            logger.info("Accediendo al endpoint '/' o '/home' desde la IP: " + request.getRemoteAddr());

            if (authentication != null) {
                logger.info("Usuario autenticado: " + authentication.getName());
            } else {
                logger.warn("No hay un usuario autenticado (authentication es null)");
            }

            return "index";

        } catch (Exception e) {
            logger.error("Error al cargar la página principal", e);
            throw e;
        }
    }
}
