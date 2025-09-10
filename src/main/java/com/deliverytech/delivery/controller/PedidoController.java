package com.deliverytech.delivery.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.deliverytech.delivery.dto.request.PedidoRequest;
import com.deliverytech.delivery.dto.response.PedidoResponse;
import com.deliverytech.delivery.model.Pedido;
import com.deliverytech.delivery.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name= "Pedidos", description = "Endpoint de Pedidos")
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Cria um pedido")
    @CachePut(value = "pedido", key = "#result.body.id")
    @CacheEvict(value = "pedidos", allEntries = true)
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = pedidoService.criar(request);
        PedidoResponse response = new PedidoResponse(
            pedido.getId(),
            pedido.getCliente().getId(),
            pedido.getRestaurante().getId(),
            pedido.getEnderecoEntrega(),
            pedido.getTotal(),
            pedido.getStatus(),
            pedido.getDataPedido(),
            pedido.getItens() 
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os pedidos")
    @Cacheable(value = "pedidos")
    public List<PedidoResponse> listar() {
        System.out.println("Buscando pedidos do banco de dados...");
        return pedidoService.listarTodos().stream()
            .map(pedido -> new PedidoResponse(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getRestaurante().getId(),
                pedido.getEnderecoEntrega(),
                pedido.getTotal(),
                pedido.getStatus(),
                pedido.getDataPedido(),
                pedido.getItens() 
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca pedido por ID")
    @Cacheable(value = "pedido", key = "#id")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
            .map(pedido -> new PedidoResponse(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getRestaurante().getId(),
                pedido.getEnderecoEntrega(),
                pedido.getTotal(),
                pedido.getStatus(),
                pedido.getDataPedido(),
                pedido.getItens()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um pedido")
    @CachePut(value = "pedido", key = "#id")
    @CacheEvict(value = "pedidos", allEntries = true)
    public ResponseEntity<PedidoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request) {
        Pedido atualizado = pedidoService.atualizar(id, request);
        PedidoResponse response = new PedidoResponse(
            atualizado.getId(),
            atualizado.getCliente().getId(),
            atualizado.getRestaurante().getId(),
            atualizado.getEnderecoEntrega(),
            atualizado.getTotal(),
            atualizado.getStatus(),
            atualizado.getDataPedido(),
            atualizado.getItens()
        );
        return ResponseEntity.ok(response);
    }
}