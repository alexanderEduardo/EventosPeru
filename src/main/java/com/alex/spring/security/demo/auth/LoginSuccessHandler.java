package com.alex.spring.security.demo.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        SessionFlashMapManager flashMapManager = new SessionFlashMapManager();

        FlashMap flashMap = new FlashMap();

        flashMap.put("success", "Hola " +authentication.getName()+ ", haz iniciado sesión con éxito!");

        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        if(authentication != null) {
            logger.info("El usuario '"+authentication.getName()+"' ha iniciado sesión con éxito");
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
