package com.tamanini.minhasfinancas.service;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.tamanini.minhasfinancas.exception.ErroAutenticacao;
import com.tamanini.minhasfinancas.exception.RegraNegocioException;
import com.tamanini.minhasfinancas.model.entity.Usuario;
import com.tamanini.minhasfinancas.service.impl.UsuarioServiceImpl;
import com.tamanini.minhasfinancas.repository.UsuarioRepository;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @SpyBean
    UsuarioServiceImpl service;

    @MockBean
    UsuarioRepository repository;

    @Test
    public void deveSalvarUmUsuario() {
        Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
        Usuario usuario = Usuario.builder().id(1L).nome("nome").email("email@email.com").senha("senha").build();

        Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

        Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

        Assertions.assertThat(usuarioSalvo).isNotNull();
        Assertions.assertThat(usuarioSalvo.getId()).isEqualTo(1l);
        Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
        Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
        Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
    }

    @Test(expected = RegraNegocioException.class)
    public void naoDeveSalvarUmUsuarioComUmEmailJaCadastrado() {
        String email = "email@email.com";
        Usuario usuario = Usuario.builder().email(email).build();
        Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

        service.salvarUsuario(usuario);

        Mockito.verify(repository, Mockito.never()).save(usuario);
    }

    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso() {
        String email = "email@email.com";
        String senha = "senha";

        Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
        Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

        Usuario result = service.autenticar(email, senha);

        Assertions.assertThat(result).isNotNull();
    }

    @Test
    public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        Throwable exception = Assertions.catchThrowable(() -> service.autenticar("email@email.com", "senha"));
        Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário não encontrado!");
    }

    @Test
    public void deveLancarErroQuandoSenhaNaoBater() {
        String senha = "senha";
        Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

        Throwable exception = Assertions.catchThrowable(() -> service.autenticar("email@email.com", "123"));
        Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha inválida!");
    }

    @Test(expected = Test.None.class)
    public void deveValidarEmail() {
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

        service.validarEmail("email@email.com");
    }

    @Test(expected = RegraNegocioException.class)
    public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

        service.validarEmail("email@email.com");
    }
}

