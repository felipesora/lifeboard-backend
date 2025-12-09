package com.lifeboard.service;

import com.lifeboard.dto.financeiro.FinanceiroRequestDTO;
import com.lifeboard.dto.transacao.TransacaoRequestDTO;
import com.lifeboard.dto.transacao.TransacaoResponseDTO;
import com.lifeboard.mapper.TransacaoMapper;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.MetaFinanceira;
import com.lifeboard.model.Transacao;
import com.lifeboard.model.enums.TipoTransacao;
import com.lifeboard.repository.MetaFinanceiraRepository;
import com.lifeboard.repository.TransacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.lifeboard.exception.BadRequestException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private FinanceiroService financeiroService;

    @Autowired
    private MetaFinanceiraRepository metaFinanceiraRepository;

    public Page<TransacaoResponseDTO> listarTodos(Pageable pageable) {
        return transacaoRepository.findAllByOrderByIdAsc(pageable)
                .map(TransacaoMapper::toDTO);
    }

    public TransacaoResponseDTO buscarDTOPorId(Long id) {
        var transacao = buscarEntidadePorId(id);

        return TransacaoMapper.toDTO(transacao);
    }

    @Transactional
    public TransacaoResponseDTO salvar(TransacaoRequestDTO transacaoDTO) {
        Financeiro financeiro = financeiroService.buscarEntidadePorId(transacaoDTO.getIdFinanceiro());

        BigDecimal saldoAtual = financeiro.getSaldoAtual();
        BigDecimal valorTransacao = transacaoDTO.getValor();

        if (transacaoDTO.getTipo() == TipoTransacao.SAIDA) {
            if (saldoAtual.compareTo(valorTransacao) < 0) {
                throw new BadRequestException("Saldo insuficiente para realizar a transação de SAIDA!");
            }
            financeiro.setSaldoAtual(saldoAtual.subtract(valorTransacao));

        } else if (transacaoDTO.getTipo() == TipoTransacao.ENTRADA) {
            financeiro.setSaldoAtual(saldoAtual.add(valorTransacao));
        }

        FinanceiroRequestDTO financeiroDTO = new FinanceiroRequestDTO();
        financeiroDTO.setSaldoAtual(financeiro.getSaldoAtual());
        financeiroDTO.setSalarioMensal(financeiro.getSalarioMensal());
        financeiroDTO.setUsuarioId(financeiro.getUsuario().getId());

        financeiroService.atualizar(financeiro.getId(), financeiroDTO);

        Transacao transacao = TransacaoMapper.toEntity(transacaoDTO, financeiro);
        var transacaoSalva = transacaoRepository.save(transacao);

        return TransacaoMapper.toDTO(transacaoSalva);
    }

    @Transactional
    public TransacaoResponseDTO atualizar(Long id, TransacaoRequestDTO transacaoDTO) {
        Transacao transacaoExistente = buscarEntidadePorId(id);

        Financeiro financeiro = transacaoExistente.getFinanceiro();
        BigDecimal saldoAtual = financeiro.getSaldoAtual();

        BigDecimal valorTransacaoAntigo = transacaoExistente.getValor();
        TipoTransacao tipoTransacaoAntigo = transacaoExistente.getTipo();

        BigDecimal valorTransacaoNovo = transacaoDTO.getValor();
        TipoTransacao tipoTransacaoNovo = transacaoDTO.getTipo();

        // Desfaz o efeito antigo
        if (tipoTransacaoAntigo == TipoTransacao.SAIDA) {
            saldoAtual = saldoAtual.add(valorTransacaoAntigo);

        } else if (tipoTransacaoAntigo == TipoTransacao.ENTRADA) {
            saldoAtual = saldoAtual.subtract(valorTransacaoAntigo);
        }

        // Aplica o efeito novo
        if (tipoTransacaoNovo == TipoTransacao.SAIDA) {
            if (saldoAtual.compareTo(valorTransacaoNovo) < 0) {
                throw new BadRequestException("Saldo insuficiente para realizar a atualização de SAIDA!");
            }
            saldoAtual = saldoAtual.subtract(valorTransacaoNovo);

        } else if (tipoTransacaoNovo == TipoTransacao.ENTRADA) {
            saldoAtual = saldoAtual.add(valorTransacaoNovo);
        }

        financeiro.setSaldoAtual(saldoAtual);

        FinanceiroRequestDTO financeiroDTO = new FinanceiroRequestDTO();
        financeiroDTO.setSaldoAtual(financeiro.getSaldoAtual());
        financeiroDTO.setSalarioMensal(financeiro.getSalarioMensal());
        financeiroDTO.setUsuarioId(financeiro.getUsuario().getId());

        financeiroService.atualizar(financeiro.getId(), financeiroDTO);

        transacaoExistente.setDescricao(transacaoDTO.getDescricao());
        transacaoExistente.setValor(valorTransacaoNovo);
        transacaoExistente.setTipo(tipoTransacaoNovo);
        transacaoExistente.setCategoria(transacaoDTO.getCategoria());

        var transacaoAtualizada = transacaoRepository.save(transacaoExistente);

        return TransacaoMapper.toDTO(transacaoAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Transacao transacao = buscarEntidadePorId(id);

        Financeiro financeiro = transacao.getFinanceiro();
        BigDecimal saldoAtual = financeiro.getSaldoAtual();
        BigDecimal valorTransacao = transacao.getValor();

        switch (transacao.getTipo()) {
            case SAIDA -> saldoAtual = saldoAtual.add(valorTransacao);

            case ENTRADA -> {
                if (saldoAtual.compareTo(valorTransacao) < 0) {
                    throw new BadRequestException("Saldo insuficiente para remover esta ENTRADA. Isso deixaria o saldo negativo!");
                }
                saldoAtual = saldoAtual.subtract(valorTransacao);
            }

            case APLICACAO -> {
                // devolve valor ao financeiro e remove da meta
                saldoAtual = saldoAtual.add(valorTransacao);

                MetaFinanceira meta = metaFinanceiraRepository.findByFinanceiroAndNome(financeiro, transacao.getDescricao().replace("Aplicação na meta: ", ""))
                        .orElseThrow(() -> new EntityNotFoundException("Meta relacionada ao investimento não encontrada."));

                BigDecimal saldoMeta = meta.getValorAtual();

                if (saldoMeta.compareTo(valorTransacao) < 0) {
                    throw new BadRequestException("A meta não possui saldo suficiente para desfazer o investimento.");
                }

                meta.setValorAtual(saldoMeta.subtract(valorTransacao));
                metaFinanceiraRepository.save(meta);
            }

            case RESGATE -> {
                // retira valor do financeiro e adiciona à meta
                if (saldoAtual.compareTo(valorTransacao) < 0) {
                    throw new BadRequestException("Saldo insuficiente para remover este RESGATE.");
                }
                saldoAtual = saldoAtual.subtract(valorTransacao);

                MetaFinanceira meta = metaFinanceiraRepository.findByFinanceiroAndNome(financeiro, transacao.getDescricao().replace("Retirada da meta: ", ""))
                        .orElseThrow(() -> new EntityNotFoundException("Meta relacionada ao resgate não encontrada."));

                meta.setValorAtual(meta.getValorAtual().add(valorTransacao));
                metaFinanceiraRepository.save(meta);
            }
        }

        financeiro.setSaldoAtual(saldoAtual);

        financeiro.getTransacoes().remove(transacao);

        transacaoRepository.delete(transacao);
        transacaoRepository.flush();
    }

    public Transacao buscarEntidadePorId(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação com id: " + id + " não encontrada"));
    }

    public List<Transacao> buscarTransacoesPorFinanceiro(Financeiro financeiro) {
        return transacaoRepository.findByFinanceiro(financeiro);
    }

    public void atualizarVariasTransacoes(List<Transacao> transacoes) {
        transacaoRepository.saveAll(transacoes);
    }

    public void deletarVariasTransacoes(List<Transacao> transacoes) {
        transacaoRepository.deleteAll(transacoes);
    }
}
