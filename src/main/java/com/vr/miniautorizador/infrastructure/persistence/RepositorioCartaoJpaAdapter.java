package com.vr.miniautorizador.infrastructure.persistence;

import com.vr.miniautorizador.domain.model.Cartao;
import com.vr.miniautorizador.domain.port.RepositorioCartao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adapter que implementa a porta de saida {@link RepositorioCartao} usando Spring Data JPA.
 * Traduz entre o agregado de dominio {@link Cartao} e a entidade de persistencia
 * {@link CartaoJpaEntity}.
 */
@Component
public class RepositorioCartaoJpaAdapter implements RepositorioCartao {

    private final CartaoJpaRepository jpaRepository;

    public RepositorioCartaoJpaAdapter(CartaoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existePorNumero(String numeroCartao) {
        return jpaRepository.existsById(numeroCartao);
    }

    @Override
    public Optional<Cartao> buscarPorNumero(String numeroCartao) {
        return jpaRepository.findById(numeroCartao).map(this::paraDominio);
    }

    @Override
    public void salvar(Cartao cartao) {
        jpaRepository.save(new CartaoJpaEntity(cartao.getNumeroCartao(), cartao.getSenhaCodificada(), cartao.getSaldo()));
    }

    @Override
    @Transactional
    public int debitarSeSaldoSuficiente(String numeroCartao, BigDecimal valor) {
        return jpaRepository.debitarSeSaldoSuficiente(numeroCartao, valor);
    }

    private Cartao paraDominio(CartaoJpaEntity entidade) {
        return new Cartao(entidade.getNumeroCartao(), entidade.getSenhaCodificada(), entidade.getSaldo());
    }
}
