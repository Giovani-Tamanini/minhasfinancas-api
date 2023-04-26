package com.tamanini.minhasfinancas.service;

import com.tamanini.minhasfinancas.model.entity.Lancamento;
import com.tamanini.minhasfinancas.model.enums.StatusLancamento;

import java.util.List;

public interface LancamentoService {

    Lancamento salvar(Lancamento lancamento);

    Lancamento atualizar(Lancamento lancamento);

    void deletar(Lancamento lancamento);

    List<Lancamento> buscar(Lancamento lancamentoFiltro);

    void atualizarStaus(Lancamento lancamento, StatusLancamento status);

    void validar(Lancamento lancamento);
}
