package com.deliverytech.delivery.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.deliverytech.delivery.dto.request.ClienteRequest;
import com.deliverytech.delivery.dto.response.ClienteResponse;
import com.deliverytech.delivery.model.Cliente;
import com.deliverytech.delivery.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name= "Clientes", description = "Endpoint de Clientes")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista de todos os clientes")
    @Cacheable(value = "clientes")
    public ResponseEntity<List<ClienteResponse>> listarTodos(Authentication authentication) {
        System.out.println("Usuário autenticado: " + authentication.getName());
        List<ClienteResponse> clientes = clienteService.listarAtivos().stream()
                .map(c -> new ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getAtivo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    @Cacheable(value = "cliente", key = "#id")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
            .map(c -> new ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getAtivo()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar cliente")
    @CacheEvict(value = "clientes", allEntries = true)
    public ResponseEntity<?> criar(@Valid @RequestBody ClienteRequest request) {
        try {
            Cliente novoCliente = clienteService.cadastrar(Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .build());
            ClienteResponse response = new ClienteResponse(novoCliente.getId(), novoCliente.getNome(), novoCliente.getEmail(), novoCliente.getAtivo());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados do cliente", description = "Atualiza dados de um cliente definindo pelo ID")
    @CachePut(value = "cliente", key = "#id")
    @CacheEvict(value = "clientes", allEntries = true)
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        try {
            Cliente atualizado = Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .build();
            Cliente salvo = clienteService.atualizar(id, atualizado);
            ClienteResponse response = new ClienteResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getAtivo());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status de um cliente")
    @CacheEvict(value = "clientes", allEntries = true)
    @CacheEvict(value = "cliente", key = "#id")
    public ResponseEntity<Void> ativarDesativar(@PathVariable Long id) {
        clienteService.ativarDesativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cliente")
    @CacheEvict(value = "clientes", allEntries = true)
    @CacheEvict(value = "cliente", key = "#id")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            clienteService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}