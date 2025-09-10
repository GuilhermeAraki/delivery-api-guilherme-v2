package com.deliverytech.delivery.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.deliverytech.delivery.dto.request.ProdutoRequest;
import com.deliverytech.delivery.dto.response.ProdutoResponse;
import com.deliverytech.delivery.model.Produto;
import com.deliverytech.delivery.model.Restaurante;
import com.deliverytech.delivery.service.ProdutoService;
import com.deliverytech.delivery.service.RestauranteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Produtos", description = "Endpoint de Produtos")
@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;
    private final RestauranteService restauranteService;

    @PostMapping
    @Operation(summary = "Cadastra um Produto")
    @CacheEvict(value = "produtos", allEntries = true)
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoRequest request) {
        Restaurante restaurante = restauranteService.buscarPorId(request.getRestauranteId())
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));

        Produto produto = Produto.builder()
                .nome(request.getNome())
                .categoria(request.getCategoria())
                .descricao(request.getDescricao())
                .preco(request.getPreco())
                .disponivel(true)
                .restaurante(restaurante)
                .build();

        Produto salvo = produtoService.cadastrar(produto);
        return ResponseEntity.ok(new ProdutoResponse(
                salvo.getId(), salvo.getNome(), salvo.getCategoria(), salvo.getDescricao(),
                salvo.getPreco(), salvo.getDisponivel()));
    }

    @GetMapping("/restaurante/{restauranteId}")
    @Operation(
        summary = "Lista Produtos por Restaurante", 
        description = "Retorna todos os produtos a partir de um ID de restaurante"
    )
    @Cacheable(value = "produtos", key = "#restauranteId")
    public List<ProdutoResponse> listarPorRestaurante(@PathVariable Long restauranteId) {
        System.out.println("Buscando produtos do banco de dados...");
        return produtoService.buscarPorRestaurante(restauranteId).stream()
            .map(p -> new ProdutoResponse(p.getId(), p.getNome(), p.getCategoria(),
                    p.getDescricao(), p.getPreco(), p.getDisponivel()))
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca produto por ID")
    @Cacheable(value = "produto", key = "#id")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id)
            .map(p -> new ProdutoResponse(p.getId(), p.getNome(), p.getCategoria(),
                    p.getDescricao(), p.getPreco(), p.getDisponivel()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualiza Produto do Restaurante", 
        description = "Atualiza um Produto do restaurante definido no ID"
    )
    @CachePut(value = "produto", key = "#id")
    @CacheEvict(value = "produtos", allEntries = true)
    public ProdutoResponse atualizar(
        @Parameter(description = "ID do Produto a ser atualizado", example = "10", required = true) @PathVariable Long id,
        @Valid @RequestBody ProdutoRequest request) {

        Produto atualizado = Produto.builder()
                .nome(request.getNome())
                .categoria(request.getCategoria())
                .descricao(request.getDescricao())
                .preco(request.getPreco())
                .build();
        Produto salvo = produtoService.atualizar(id, atualizado);
        return new ProdutoResponse(salvo.getId(), salvo.getNome(), salvo.getCategoria(),
                salvo.getDescricao(), salvo.getPreco(), salvo.getDisponivel());
    }

    @PatchMapping("/{id}/disponibilidade")
    @Operation(
        summary = "Alterna a disponibilidade de um Produto"
    )
    @CacheEvict(value = "produtos", allEntries = true)
    @CacheEvict(value = "produto", key = "#id")
    public ResponseEntity<Void> alterarDisponibilidade(@PathVariable Long id, @RequestParam boolean disponivel){
        produtoService.alterarDisponibilidade(id, disponivel);
        return ResponseEntity.noContent().build();
    }
}