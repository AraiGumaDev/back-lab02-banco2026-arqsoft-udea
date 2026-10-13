package com.ejemplo.banco.banco2026.controller;

import com.ejemplo.banco.banco2026.dto.TransactionDTO;
import com.ejemplo.banco.banco2026.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransferController {
    private final TransactionService transactionFacade;
    public TransferController(TransactionService transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionFacade.getAlltransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable long id) {
        return ResponseEntity.ok(transactionFacade.getTransactionById(id));
    }

    @PostMapping()
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDTO) {
       TransactionDTO createTransaction = transactionFacade.transferMoney(transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createTransaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransactionById(@PathVariable long id, @RequestBody TransactionDTO transactionDTO) {
        return ResponseEntity.ok(transactionFacade.updateTransactionById(id, transactionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransactionById(@PathVariable long id){
        transactionFacade.deleteTransactionById(id);
        return ResponseEntity.noContent().build();
    }

}
