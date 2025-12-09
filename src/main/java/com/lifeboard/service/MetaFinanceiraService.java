package com.lifeboard.service;

import com.lifeboard.dto.financeiro.FinanceiroRequestDTO;
import com.lifeboard.dto.meta.MetaFinanceiraResponseDTO;
import com.lifeboard.dto.meta.MetaFinanceiraSaveRequestDTO;
import com.lifeboard.dto.meta.MetaFinanceiraUpdateRequestDTO;
import com.lifeboard.dto.transacao.TransacaoRequestDTO;
import com.lifeboard.exception.BadRequestException;
import com.lifeboard.mapper.MetaFinanceiraMapper;
import com.lifeboard.model.Financeiro;
import com.lifeboard.model.MetaFinanceira;
import com.lifeboard.model.Transacao;
import com.lifeboard.model.enums.CategoriaTransacao;
import com.lifeboard.model.enums.StatusMeta;
import com.lifeboard.model.enums.TipoTransacao;
import com.lifeboard.repository.MetaFinanceiraRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MetaFinanceiraService {

    @Autowired
    private MetaFinanceiraRepository metaRepository;

    @Autowired
    private FinanceiroService financeiroService;

    @Autowired
    private TransacaoService transacaoService;

    public Page<MetaFinanceiraResponseDTO> listarTodos(Pageable pageable) {
        return metaRepository.findAllByOrderByIdAsc(pageable)
                .map(MetaFinanceiraMapper::toDTO);
    }

    public MetaFinanceiraResponseDTO buscarDTOPorId(Long id) {
        var meta = buscarEntidadePorId(id);

        return MetaFinanceiraMapper.toDTO(meta);
    }

    @Transactional
    public MetaFinanceiraResponseDTO salvar(MetaFinanceiraSaveRequestDTO metaFinanceiraDTO) {
        Financeiro financeiro = financeiroService.buscarEntidadePorId(metaFinanceiraDTO.getIdFinanceiro());

        BigDecimal saldoAtual = financeiro.getSaldoAtual();
        BigDecimal valorMetaAtual = metaFinanceiraDTO.getValorAtual();

        if (saldoAtual.compareTo(valorMetaAtual) < 0) {
            throw new BadRequestException("Saldo insuficiente para criar esta Meta Financeira!");
        }

        financeiro.setSaldoAtual(saldoAtual.subtract(valorMetaAtual));

        FinanceiroRequestDTO financeiroDTO = new FinanceiroRequestDTO();
        financeiroDTO.setSaldoAtual(financeiro.getSaldoAtual());
        financeiroDTO.setSalarioMensal(financeiro.getSalarioMensal());
        financeiroDTO.setUsuarioId(financeiro.getUsuario().getId());

        financeiroService.atualizar(financeiro.getId(), financeiroDTO);

        MetaFinanceira metaFinanceira = MetaFinanceiraMapper.toEntitySave(metaFinanceiraDTO, financeiro);

        definirStatusMetaFinanceiro(metaFinanceira);

        return MetaFinanceiraMapper.toDTO(metaRepository.save(metaFinanceira));
    }

    @Transactional
    public void adicionarSaldo(Long metaId, BigDecimal valor) {
        MetaFinanceira meta = buscarEntidadePorId(metaId);
        Financeiro financeiro = meta.getFinanceiro();
        BigDecimal saldoFinanceiro = financeiro.getSaldoAtual();

        if (saldoFinanceiro.compareTo(valor) < 0) {
            throw new BadRequestException("Saldo insuficiente para realizar a adição de saldo à meta financeira!");
        }

        meta.setValorAtual(meta.getValorAtual().add(valor));
        definirStatusMetaFinanceiro(meta);
        metaRepository.save(meta);

        TransacaoRequestDTO transacaoDTO = new TransacaoRequestDTO();
        transacaoDTO.setDescricao("Aplicação na meta: " + meta.getNome());
        transacaoDTO.setValor(valor);
        transacaoDTO.setTipo(TipoTransacao.APLICACAO);
        transacaoDTO.setCategoria(CategoriaTransacao.INVESTIMENTO);
        transacaoDTO.setIdFinanceiro(financeiro.getId());
        transacaoService.salvar(transacaoDTO);

        financeiro.setSaldoAtual(saldoFinanceiro.subtract(valor));

        FinanceiroRequestDTO financeiroDTO = new FinanceiroRequestDTO();
        financeiroDTO.setSaldoAtual(financeiro.getSaldoAtual());
        financeiroDTO.setSalarioMensal(financeiro.getSalarioMensal());
        financeiroDTO.setUsuarioId(financeiro.getUsuario().getId());

        financeiroService.atualizar(financeiro.getId(), financeiroDTO);
    }

    @Transactional
    public void retirarSaldo(Long metaId, BigDecimal valor) {
        MetaFinanceira meta = buscarEntidadePorId(metaId);

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor a ser retirado deve ser maior que zero.");
        }

        if (meta.getValorAtual().compareTo(valor) < 0) {
            throw new BadRequestException("Saldo insuficiente na meta.");
        }

        meta.setValorAtual(meta.getValorAtual().subtract(valor));
        definirStatusMetaFinanceiro(meta);
        metaRepository.save(meta);

        Financeiro financeiro = meta.getFinanceiro();
        BigDecimal saldoFinanceiro = financeiro.getSaldoAtual();

        TransacaoRequestDTO transacaoDTO = new TransacaoRequestDTO();
        transacaoDTO.setDescricao("Retirada da meta: " + meta.getNome());
        transacaoDTO.setValor(valor);
        transacaoDTO.setTipo(TipoTransacao.RESGATE);
        transacaoDTO.setCategoria(CategoriaTransacao.INVESTIMENTO);
        transacaoDTO.setIdFinanceiro(financeiro.getId());
        transacaoService.salvar(transacaoDTO);

        financeiro.setSaldoAtual(saldoFinanceiro.add(valor));

        FinanceiroRequestDTO financeiroDTO = new FinanceiroRequestDTO();
        financeiroDTO.setSaldoAtual(financeiro.getSaldoAtual());
        financeiroDTO.setSalarioMensal(financeiro.getSalarioMensal());
        financeiroDTO.setUsuarioId(financeiro.getUsuario().getId());

        financeiroService.atualizar(financeiro.getId(), financeiroDTO);
    }

    @Transactional
    public MetaFinanceiraResponseDTO atualizar(Long id, MetaFinanceiraUpdateRequestDTO metaFinanceiraDTO) {
        MetaFinanceira metaExistente = buscarEntidadePorId(id);

        String nomeMetaAntigo = metaExistente.getNome();
        String nomeMetaNovo = metaFinanceiraDTO.getNome();

        metaExistente.setNome(metaFinanceiraDTO.getNome());
        metaExistente.setValorMeta(metaFinanceiraDTO.getValorMeta());
        metaExistente.setDataLimite(metaFinanceiraDTO.getDataLimite());
        definirStatusMetaFinanceiro(metaExistente);

        // Atualizar descrições das transações relacionadas
        List<Transacao> transacoes = transacaoService.buscarTransacoesPorFinanceiro(metaExistente.getFinanceiro());
        for (Transacao t : transacoes) {
            if (t.getDescricao().equals("Aplicação na meta: " + nomeMetaAntigo)) {
                t.setDescricao("Aplicação na meta: " + nomeMetaNovo);
            } else if (t.getDescricao().equals("Retirada da meta: " + nomeMetaAntigo)) {
                t.setDescricao("Retirada da meta: " + nomeMetaNovo);
            }
        }
        transacaoService.atualizarVariasTransacoes(transacoes);

        var metaAtualizada = metaRepository.save(metaExistente);

        return MetaFinanceiraMapper.toDTO(metaAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        MetaFinanceira meta = buscarEntidadePorId(id);

        Financeiro financeiro = meta.getFinanceiro();
        BigDecimal saldoAtual = financeiro.getSaldoAtual();
        BigDecimal valorMetaAtual = meta.getValorAtual();

        financeiro.setSaldoAtual(saldoAtual.add(valorMetaAtual));

        // Buscar transações relacionadas com a meta (pela descrição)
        List<Transacao> transacoesRelacionadas = transacaoService.buscarTransacoesPorFinanceiro(financeiro)
                .stream()
                .filter(t -> t.getDescricao().contains(meta.getNome()))
                .toList();

        // Deletar as transações relacionadas
        transacaoService.deletarVariasTransacoes(transacoesRelacionadas);

        metaRepository.deleteById(id);
    }

    public MetaFinanceira buscarEntidadePorId(Long id) {
        return metaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meta Financeira com id: " + id + " não encontrada"));
    }

    public void definirStatusMetaFinanceiro(MetaFinanceira metaFinanceira) {

        if (metaFinanceira.getValorAtual().compareTo(metaFinanceira.getValorMeta()) >= 0) {
            metaFinanceira.setStatus(StatusMeta.CONCLUIDA);

        } else {
            metaFinanceira.setStatus(StatusMeta.EM_ANDAMENTO);
        }
    }
}
