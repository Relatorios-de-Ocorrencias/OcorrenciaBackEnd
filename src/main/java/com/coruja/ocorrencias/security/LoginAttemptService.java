package com.coruja.ocorrencias.security;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int LIMITE_FALHAS = 10;
    private static final Duration JANELA = Duration.ofMinutes(10);
    private static final Duration BLOQUEIO = Duration.ofMinutes(10);

    private final Map<String, Tentativas> registros = new HashMap<>();

    public synchronized boolean estaBloqueado(String email) {
        Instant agora = Instant.now();
        removerExpirados(agora);

        Tentativas registro = registros.get(email);

        return registro != null
                && registro.bloqueadoAte != null
                && agora.isBefore(registro.bloqueadoAte);
    }

    public synchronized void registrarFalha(String email) {
        Instant agora = Instant.now();
        removerExpirados(agora);

        Tentativas registro = registros.computeIfAbsent(
                email,
                chave -> new Tentativas(agora)
        );

        if (registro.bloqueadoAte != null) {
            return;
        }

        registro.falhas++;

        if (registro.falhas >= LIMITE_FALHAS) {
            registro.bloqueadoAte = agora.plus(BLOQUEIO);
        }
    }

    public synchronized void limparFalhas(String email) {
        registros.remove(email);
    }

    private void removerExpirados(Instant agora) {
        registros.values().removeIf(registro -> {
            Instant expiracao = registro.bloqueadoAte != null
                    ? registro.bloqueadoAte
                    : registro.inicio.plus(JANELA);

            return !agora.isBefore(expiracao);
        });
    }

    private static class Tentativas {

        private int falhas;
        private final Instant inicio;
        private Instant bloqueadoAte;

        private Tentativas(Instant inicio) {
            this.inicio = inicio;
        }
    }
}