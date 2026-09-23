package com.vr.miniautorizador.domain.port;

/**
 * Porta de saida (driven port) para codificacao e verificacao de senha do cartao.
 * <p>
 * Definida no dominio para que as regras de autorizacao (que sao regras de negocio)
 * nao dependam de nenhum detalhe de infraestrutura (ex.: BCrypt/Spring Security).
 * A implementacao concreta vive na camada de infraestrutura.
 */
public interface CodificadorDeSenha {

    String codificar(String senhaPura);

    boolean confere(String senhaPura, String senhaCodificada);
}
