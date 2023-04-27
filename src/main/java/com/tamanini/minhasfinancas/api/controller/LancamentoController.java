package com.tamanini.minhasfinancas.api.controller;

import com.tamanini.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.tamanini.minhasfinancas.api.dto.LancamentoDTO;
import com.tamanini.minhasfinancas.exception.RegraNegocioException;
import com.tamanini.minhasfinancas.model.entity.Lancamento;
import com.tamanini.minhasfinancas.model.entity.Usuario;
import com.tamanini.minhasfinancas.model.enums.StatusLancamento;
import com.tamanini.minhasfinancas.model.enums.TipoLancamento;
import com.tamanini.minhasfinancas.service.LancamentoService;
import com.tamanini.minhasfinancas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoController {

    private final LancamentoService service;

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity buscar(@RequestParam(value = "descricao", required = false) String descricao,
                                 @RequestParam(value = "mes", required = false) Integer mes,
                                 @RequestParam(value = "ano", required = false) Integer ano,
                                 @RequestParam("usuarioID") Long usuarioID) {
        Lancamento lancamentoFiltro = new Lancamento();
        lancamentoFiltro.setDescricao(descricao);
        lancamentoFiltro.setMes(mes);
        lancamentoFiltro.setAno(ano);

        Optional<Usuario> usuario = usuarioService.obterPorId(usuarioID);
        if (!usuario.isPresent()) {
            return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuário não encontrado!");
        } else {
            lancamentoFiltro.setUsuario(usuario.get());
        }

        List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
        return ResponseEntity.ok(lancamentos);
    }

    @PostMapping
    public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento entidade = construirLancamento(dto);
            entidade = service.salvar(entidade);

            return new ResponseEntity(entidade, HttpStatus.CREATED);
        } catch (RegraNegocioException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @PutMapping("{id}")
    public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
        return service.obterPorId(id).map(entity -> {
            try {
                Lancamento lancamento = construirLancamento(dto);
                lancamento.setId(entity.getId());

                service.atualizar(lancamento);
                return ResponseEntity.ok(lancamento);
            } catch (RegraNegocioException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado!", HttpStatus.BAD_REQUEST));
    }

    @PutMapping("{id}/atualizarStatus")
    public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto) {
        return service.obterPorId(id).map(entity -> {
            StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());
            try {
                entity.setStatus(statusSelecionado);
                service.atualizar(entity);
                return ResponseEntity.ok(entity);
            } catch (RegraNegocioException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado!", HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id) {
        return service.obterPorId(id).map(entity -> {
            service.deletar(entity);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado!", HttpStatus.BAD_REQUEST));
    }

    private Lancamento construirLancamento(LancamentoDTO dto) {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId());
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setAno(dto.getAno());
        lancamento.setMes(dto.getMes());
        lancamento.setValor(dto.getValor());

        Usuario usuario = usuarioService.obterPorId(dto.getUsuarioID())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado!"));

        lancamento.setUsuario(usuario);

        if (dto.getTipo() != null) {
            lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
        }

        if (dto.getStatus() != null) {
            lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
        }
        return lancamento;
    }
}
