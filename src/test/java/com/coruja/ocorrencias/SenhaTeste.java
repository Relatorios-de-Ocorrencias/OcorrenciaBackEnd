package com.coruja.ocorrencias;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SenhaTeste {

    @Test
    void gerarEVerificarHash() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String senha = "TesteLocal#2026";
        String hash = encoder.encode(senha);

        assertTrue(encoder.matches(senha, hash));
        assertFalse(encoder.matches("SenhaIncorreta", hash));

        System.out.println("Hash para conta local: " + hash);
    }
}