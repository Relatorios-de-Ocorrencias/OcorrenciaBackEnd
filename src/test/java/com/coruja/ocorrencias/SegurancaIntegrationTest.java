package com.coruja.ocorrencias;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.coruja.ocorrencias.entity.*;
import com.coruja.ocorrencias.entity.usuarios.*;
import com.coruja.ocorrencias.repository.*;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired OcorrenciaRepository ocorrencias;
    @Autowired ObservacaoRepository observacoes;
    @Autowired FotoRepository fotos;
    @Autowired PasswordEncoder encoder;

    private Usuario joao;
    private Usuario maria;
    private Usuario admin;
    private OcorrenciaEntity propria;
    private OcorrenciaEntity alheia;
    private String senha = "SenhaFicticia#2026";

    @BeforeEach
    void preparar() {
        String identificador = UUID.randomUUID().toString();
        joao = criarUsuario("joao-" + identificador + "@example.com", Role.USER);
        maria = criarUsuario("maria-" + identificador + "@example.com", Role.USER);
        admin = criarUsuario("admin-" + identificador + "@example.com", Role.ADMIN);
        propria = criarOcorrencia(joao);
        alheia = criarOcorrencia(maria);
    }

    @Test
    void loginMantemSessaoERenovaTokenELogoutInvalida() throws Exception {
        Token antes = token(null);
        String idAntes = antes.session().getId();
        MvcResult resultado = mvc.perform(post("/login").session(antes.session())
                .header(antes.header(), antes.valor()).param("email", joao.getEmail())
                .param("password", senha)).andExpect(status().isNoContent()).andReturn();
        MockHttpSession sessao = (MockHttpSession) resultado.getRequest().getSession(false);
        assertNotEquals(idAntes, sessao.getId());
        mvc.perform(get("/auth/me").session(sessao))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(joao.getEmail()))
                .andExpect(jsonPath("$.senha").doesNotExist());
        mvc.perform(post("/ocorrencias/" + propria.getId() + "/fechar").session(sessao)
                .header(antes.header(), antes.valor())).andExpect(status().isForbidden());
        Token depois = token(sessao);
        mvc.perform(post("/logout").session(sessao).header(depois.header(), depois.valor()))
                .andExpect(status().isNoContent());
        assertTrue(sessao.isInvalid());
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void exigeSessaoECsrfEFiltraProprietario() throws Exception {
        mvc.perform(get("/ocorrencias/" + propria.getId())).andExpect(status().isUnauthorized());
        MockHttpSession sessao = login(joao);
        mvc.perform(get("/ocorrencias/" + propria.getId()).session(sessao)).andExpect(status().isOk());
        mvc.perform(get("/ocorrencias/" + alheia.getId()).session(sessao)).andExpect(status().isNotFound());
        mvc.perform(get("/ocorrencias/buscarUltimasTresOcorrencia").session(sessao))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(propria.getId()));
        mvc.perform(post("/ocorrencias/" + propria.getId() + "/fechar").session(sessao))
                .andExpect(status().isForbidden());
    }

    @Test
    void fecharReabrirEditarEExcluirRespeitamProprietario() throws Exception {
        MockHttpSession sessao = login(joao);
        Token token = token(sessao);
        String base = "/ocorrencias/" + propria.getId();
        mvc.perform(post(base + "/fechar").session(sessao).header(token.header(), token.valor()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FINALIZADA"));
        mvc.perform(put(base).session(sessao).header(token.header(), token.valor())
                .contentType(MediaType.APPLICATION_JSON).content(dadosOcorrencia()))
                .andExpect(status().isBadRequest());
        mvc.perform(post(base + "/reabrir").session(sessao).header(token.header(), token.valor()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DRAFT"));
        mvc.perform(put(base).session(sessao).header(token.header(), token.valor())
                .contentType(MediaType.APPLICATION_JSON).content(dadosOcorrencia()))
                .andExpect(status().isOk());
        mvc.perform(delete("/ocorrencias/" + alheia.getId()).session(sessao)
                .header(token.header(), token.valor())).andExpect(status().isNotFound());
        mvc.perform(delete(base).session(sessao).header(token.header(), token.valor()))
                .andExpect(status().isNoContent());
        assertFalse(ocorrencias.existsById(propria.getId()));
        assertTrue(ocorrencias.existsById(alheia.getId()));
    }

    @Test
    void criacaoUsaDonoDaSessao() throws Exception {
        MockHttpSession sessao = login(joao);
        Token token = token(sessao);
        MvcResult resultado = mvc.perform(post("/ocorrencias").session(sessao)
                .header(token.header(), token.valor()).contentType(MediaType.APPLICATION_JSON)
                .content(dadosOcorrencia())).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
        assertTrue(ocorrencias.findByIdAndUsuario_Id(id, joao.getId()).isPresent());
        assertTrue(ocorrencias.findByIdAndUsuario_Id(id, maria.getId()).isEmpty());
    }

    @Test
    void observacoesEFotosNaoVazamPorIdOuUrl() throws Exception {
        ObservacaoOcorrenciaEntity obs = new ObservacaoOcorrenciaEntity();
        obs.setTitulo("Observacao privada");
        obs.setOcorrencia(alheia);
        obs = observacoes.save(obs);
        FotoOcorrenciaEntity foto = new FotoOcorrenciaEntity();
        String nome = UUID.randomUUID() + ".png";
        foto.setUrlFoto("ocorrencias/" + nome);
        foto.setObservacao(obs);
        fotos.save(foto);
        MockHttpSession sessao = login(joao);
        mvc.perform(get("/ocorrencias/" + alheia.getId() + "/observacoes").session(sessao))
                .andExpect(status().isNotFound());
        mvc.perform(get("/ocorrencias/" + propria.getId() + "/observacoes/" + obs.getId()).session(sessao))
                .andExpect(status().isNotFound());
        mvc.perform(get("/arquivos/" + nome).session(sessao)).andExpect(status().isNotFound());
    }

    @Test
    void adminPodeEditarFechadaEUsuarioNaoPodeAdministrar() throws Exception {
        alheia.setStatus(StatusOcorrenciaEntity.FINALIZADA);
        ocorrencias.save(alheia);
        MockHttpSession comum = login(joao);
        mvc.perform(get("/admin/usuarios").session(comum)).andExpect(status().isForbidden());
        MockHttpSession sessao = login(admin);
        Token token = token(sessao);
        mvc.perform(put("/ocorrencias/" + alheia.getId()).session(sessao)
                .header(token.header(), token.valor()).contentType(MediaType.APPLICATION_JSON)
                .content(dadosOcorrencia())).andExpect(status().isOk());
        String email = UUID.randomUUID() + "@example.com";
        MvcResult resultado = mvc.perform(post("/admin/usuarios").session(sessao)
                .header(token.header(), token.valor()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Nova conta\",\"email\":\"" + email
                        + "\",\"senha\":\"" + senha + "\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.senha").doesNotExist()).andReturn();
        assertTrue(encoder.matches(senha, usuarios.findByEmail(email).orElseThrow().getSenha()));
        assertFalse(resultado.getResponse().getContentAsString().contains(senha));
        mvc.perform(put("/admin/usuarios/" + admin.getId()).session(sessao)
                .header(token.header(), token.valor()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"USER\",\"ativo\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mudancaDePerfilEDesativacaoValemParaSessaoExistente() throws Exception {
        MockHttpSession sessao = login(admin);
        admin.setRole(Role.USER);
        usuarios.save(admin);
        mvc.perform(get("/admin/usuarios").session(sessao)).andExpect(status().isForbidden());
        admin.setAtivo(false);
        usuarios.save(admin);
        mvc.perform(get("/auth/me").session(sessao)).andExpect(status().isForbidden());
    }

    @Test
    void dezFalhasBloqueiamInclusiveSenhaCorreta() throws Exception {
        Token token = token(null);
        for (int i = 0; i < 9; i++) {
            mvc.perform(post("/login").session(token.session()).header(token.header(), token.valor())
                    .param("email", joao.getEmail()).param("password", "incorreta"))
                    .andExpect(status().isUnauthorized());
        }
        MvcResult sucesso = mvc.perform(post("/login").session(token.session())
                .header(token.header(), token.valor()).param("email", joao.getEmail())
                .param("password", senha)).andExpect(status().isNoContent()).andReturn();
        token = token((MockHttpSession) sucesso.getRequest().getSession(false));
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/login").session(token.session()).header(token.header(), token.valor())
                    .param("email", joao.getEmail()).param("password", "incorreta"))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/login").session(token.session()).header(token.header(), token.valor())
                .param("email", joao.getEmail()).param("password", senha))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contaInativaECredenciaisLongasRetornamErroGenerico() throws Exception {
        joao.setAtivo(false);
        usuarios.save(joao);
        Token token = token(null);
        mvc.perform(post("/login").session(token.session()).header(token.header(), token.valor())
                .param("email", joao.getEmail()).param("password", senha))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Nao foi possivel autenticar"));
        mvc.perform(post("/login").session(token.session()).header(token.header(), token.valor())
                .param("email", maria.getEmail()).param("password", "x".repeat(100)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPermiteSomenteOrigemConfigurada() throws Exception {
        mvc.perform(options("/login").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,x-csrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        mvc.perform(options("/login").header("Origin", "https://origem-invalida.example")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    private Token token(MockHttpSession sessao) throws Exception {
        var request = get("/auth/csrf");
        if (sessao != null) request.session(sessao);
        MvcResult resultado = mvc.perform(request).andExpect(status().isOk()).andReturn();
        JsonNode body = json.readTree(resultado.getResponse().getContentAsString());
        return new Token((MockHttpSession) resultado.getRequest().getSession(false),
                body.get("headerName").asText(), body.get("token").asText());
    }

    private MockHttpSession login(Usuario usuario) throws Exception {
        Token token = token(null);
        MvcResult resultado = mvc.perform(post("/login").session(token.session())
                .header(token.header(), token.valor()).param("email", usuario.getEmail())
                .param("password", senha)).andExpect(status().isNoContent()).andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private Usuario criarUsuario(String email, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome("Conta de teste");
        usuario.setEmail(email);
        usuario.setSenha(encoder.encode(senha));
        usuario.setRole(role);
        usuario.setAtivo(true);
        return usuarios.save(usuario);
    }

    private OcorrenciaEntity criarOcorrencia(Usuario usuario) {
        OcorrenciaEntity ocorrencia = new OcorrenciaEntity();
        ocorrencia.setUsuario(usuario);
        ocorrencia.setTitulo("Relatorio de teste");
        ocorrencia.setCliente("Cliente");
        ocorrencia.setLocalizacao("Local");
        ocorrencia.setResponsavel("Responsavel");
        ocorrencia.setData_inspecao(LocalDate.of(2026, 1, 1));
        ocorrencia.setRevisao("1");
        return ocorrencias.save(ocorrencia);
    }

    private String dadosOcorrencia() {
        return """
            {"titulo":"Relatorio","cliente":"Cliente","localizacao":"Local",
             "setor":"Setor","area":"Area","departamento":"Departamento",
             "responsavel":"Responsavel","data_inspecao":"2026-01-01","revisao":"1"}
            """;
    }

    private record Token(MockHttpSession session, String header, String valor) {}
}
