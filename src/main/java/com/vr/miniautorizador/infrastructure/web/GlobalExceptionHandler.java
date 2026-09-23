package com.vr.miniautorizador.infrastructure.web;

import com.vr.miniautorizador.domain.exception.AutorizacaoIndisponivelException;
import com.vr.miniautorizador.domain.exception.CartaoJaExisteException;
import com.vr.miniautorizador.domain.exception.CartaoNaoEncontradoException;
import com.vr.miniautorizador.infrastructure.web.dto.CartaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traducao centralizada das excecoes de dominio/aplicacao para respostas HTTP,
 * conforme os contratos REST definidos no enunciado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartaoJaExisteException.class)
    public ResponseEntity<CartaoResponse> tratarCartaoJaExiste(CartaoJaExisteException ex) {
        return ResponseEntity.unprocessableEntity().body(new CartaoResponse(ex.getSenha(), ex.getNumeroCartao()));
    }

    @ExceptionHandler(CartaoNaoEncontradoException.class)
    public ResponseEntity<Void> tratarCartaoNaoEncontrado(CartaoNaoEncontradoException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(AutorizacaoIndisponivelException.class)
    public ResponseEntity<String> tratarAutorizacaoIndisponivel(AutorizacaoIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("AUTORIZACAO_INDISPONIVEL");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> tratarRequisicaoInvalida(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("REQUISICAO_INVALIDA");
    }
}
