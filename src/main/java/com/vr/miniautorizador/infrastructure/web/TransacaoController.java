package com.vr.miniautorizador.infrastructure.web;

import com.vr.miniautorizador.application.port.in.SolicitarAutorizacaoTransacaoUseCase;
import com.vr.miniautorizador.domain.model.ResultadoAutorizacao;
import com.vr.miniautorizador.domain.model.SolicitacaoTransacao;
import com.vr.miniautorizador.infrastructure.web.dto.TransacaoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final SolicitarAutorizacaoTransacaoUseCase solicitarAutorizacaoTransacaoUseCase;

    public TransacaoController(SolicitarAutorizacaoTransacaoUseCase solicitarAutorizacaoTransacaoUseCase) {
        this.solicitarAutorizacaoTransacaoUseCase = solicitarAutorizacaoTransacaoUseCase;
    }

    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> autorizar(@Valid @RequestBody TransacaoRequest request) {
        ResultadoAutorizacao resultado = solicitarAutorizacaoTransacaoUseCase.autorizar(
                new SolicitacaoTransacao(request.numeroCartao(), request.senhaCartao(), request.valor()));

        return resultado.isAprovada()
                ? ResponseEntity.status(HttpStatus.CREATED).body("OK")
                : ResponseEntity.unprocessableEntity().body(resultado.name());
    }
}
