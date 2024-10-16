package com.alex.spring.security.demo.controllers;

import com.alex.spring.security.demo.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
public class MainController {
    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private IUserService userService;

    @RequestMapping(value = { "/","/home"}, method = RequestMethod.GET)
    public String home(Model model, Authentication authentication, HttpServletRequest request) {
        if(authentication != null) {
            logger.info("Hola usuario autenticado, tu username es: ".concat(authentication.getName()));
        }else{
            logger.info("authentication es null");
        }

        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", userService.findAllClients());
        return "index";
    }


}
