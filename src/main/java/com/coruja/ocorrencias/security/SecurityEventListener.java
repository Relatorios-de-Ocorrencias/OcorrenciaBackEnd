package com.coruja.ocorrencias.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(SecurityEventListener.class);

    @EventListener
    public void aoAutenticar(AuthenticationSuccessEvent evento) {
        log.info("Autenticacao realizada com sucesso");
    }

    @EventListener
    public void aoFalharAutenticacao(
            AbstractAuthenticationFailureEvent evento) {

        log.warn(
            "Falha de autenticacao: tipo={}",
            evento.getException().getClass().getSimpleName()
        );
    }
}