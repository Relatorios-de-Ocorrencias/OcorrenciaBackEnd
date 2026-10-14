package com.coruja.ocorrencias.security;

import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Conecta a autenticacao BCrypt ao contador de falhas desta instancia. */
public class LoginAuthenticationProvider extends DaoAuthenticationProvider {
    private final LoginAttemptService tentativas;

    public LoginAuthenticationProvider(UserDetailsService service,
            PasswordEncoder encoder, LoginAttemptService tentativas) {
        super(service);
        setPasswordEncoder(encoder);
        this.tentativas = tentativas;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String chave = authentication.getName().trim().toLowerCase(Locale.ROOT);
        if (chave.isBlank() || chave.length() > 255 || tentativas.estaBloqueado(chave)) {
            throw new LockedException("Autenticacao temporariamente indisponivel");
        }
        try {
            Object credenciais = authentication.getCredentials();
            if (!(credenciais instanceof String senha)
                    || senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
                throw new BadCredentialsException("Credenciais invalidas");
            }
            Authentication resultado = super.authenticate(authentication);
            tentativas.limparFalhas(chave);
            return resultado;
        } catch (BadCredentialsException ex) {
            tentativas.registrarFalha(chave);
            throw ex;
        }
    }
}
