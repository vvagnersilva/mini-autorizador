package com.vr.miniautorizador.infrastructure.web;

import com.vr.miniautorizador.application.dto.CartaoCriado;
import com.vr.miniautorizador.application.dto.CriarCartaoComando;
import com.vr.miniautorizador.application.port.in.ConsultarSaldoUseCase;
import com.vr.miniautorizador.application.port.in.CriarCartaoUseCase;
import com.vr.miniautorizador.infrastructure.web.dto.CartaoResponse;
import com.vr.miniautorizador.infrastructure.web.dto.CriarCartaoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cartoes")
public class CartaoController {

    private final CriarCartaoUseCase criarCartaoUseCase;
    private final ConsultarSaldoUseCase consultarSaldoUseCase;

    public CartaoController(CriarCartaoUseCase criarCartaoUseCase, ConsultarSaldoUseCase consultarSaldoUseCase) {
        this.criarCartaoUseCase = criarCartaoUseCase;
        this.consultarSaldoUseCase = consultarSaldoUseCase;
    }

    @PostMapping
    public ResponseEntity<CartaoResponse> criar(@Valid @RequestBody CriarCartaoRequest request) {
        CartaoCriado cartaoCriado = criarCartaoUseCase.criar(new CriarCartaoComando(request.numeroCartao(), request.senha()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CartaoResponse(cartaoCriado.senha(), cartaoCriado.numeroCartao()));
    }

    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> consultarSaldo(@PathVariable String numeroCartao) {
        return ResponseEntity.ok(consultarSaldoUseCase.consultarSaldo(numeroCartao));
    }
}
