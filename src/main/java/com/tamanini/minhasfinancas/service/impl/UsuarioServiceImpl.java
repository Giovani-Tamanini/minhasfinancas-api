package com.tamanini.minhasfinancas.service.impl;

import java.util.Optional;

import javax.transaction.Transactional;

import com.tamanini.minhasfinancas.exception.ErroAutenticacao;
import com.tamanini.minhasfinancas.exception.RegraNegocioException;
import com.tamanini.minhasfinancas.model.entity.Usuario;
import com.tamanini.minhasfinancas.repository.UsuarioRepository;
import com.tamanini.minhasfinancas.service.UsuarioService;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioServiceImpl(UsuarioRepository repository) {
        super();
        this.repository = repository;
    }

    @Override
    public Usuario autenticar(String email, String senha) {
        Optional<Usuario> usuario = repository.findByEmail(email);

        if (!usuario.isPresent()) {
            throw new ErroAutenticacao("Usuário não encontrado!");
        }

        if (!usuario.get().getSenha().equals(senha)) {
            throw new ErroAutenticacao("Senha inválida!");
        }

        return usuario.get();
    }

    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        validarEmail(usuario.getEmail());
        return repository.save(usuario);
    }

    @Override
    public void validarEmail(String email) {
        boolean existe = repository.existsByEmail(email);
        if (existe) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com esse email!");
        }
    }

}
