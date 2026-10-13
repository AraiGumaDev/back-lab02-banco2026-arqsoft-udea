package com.ejemplo.banco.banco2026.service;

import com.ejemplo.banco.banco2026.dto.TransactionDTO;
import com.ejemplo.banco.banco2026.entity.Customer;
import com.ejemplo.banco.banco2026.entity.Transaction;
import com.ejemplo.banco.banco2026.exception.ResourceNotFoundException;
import com.ejemplo.banco.banco2026.mapper.TransactionMapper;
import com.ejemplo.banco.banco2026.repository.CustomerRepository;
import com.ejemplo.banco.banco2026.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CustomerRepository customerRepository;
    // Se usa el mapper real generado por MapStruct
    @Spy
    private TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

    @InjectMocks
    private TransactionService transactionService;

    private Customer customer(String account, double balance) {
        return new Customer(null, account, "Nombre", "Apellido", balance);
    }

    private Transaction transaction(Long id, String sender, String receiver, double amount) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setSenderAccountNumber(sender);
        t.setReceiverAccountNumber(receiver);
        t.setAmount(amount);
        t.setTimestamp(LocalDateTime.now());
        return t;
    }

    private TransactionDTO request(String sender, String receiver, Double amount) {
        return new TransactionDTO(null, sender, receiver, amount, null);
    }

    private void givenAccounts(Customer sender, Customer receiver) {
        when(customerRepository.findByAccountNumber(sender.getAccountNumber())).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber(receiver.getAccountNumber())).thenReturn(Optional.of(receiver));
    }

    // ---------- transferMoney ----------

    @Test
    void transferMoneyMovesBalanceAndSavesTransaction() {
        Customer sender = customer("111", 100.0);
        Customer receiver = customer("222", 50.0);
        givenAccounts(sender, receiver);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TransactionDTO result = transactionService.transferMoney(request("111", "222", 30.0));

        assertEquals(70.0, sender.getBalance());
        assertEquals(80.0, receiver.getBalance());
        assertEquals(10L, result.getId());
        assertEquals(30.0, result.getAmount());
        verify(customerRepository).save(sender);
        verify(customerRepository).save(receiver);
    }

    @Test
    void transferMoneyRejectsNullAccounts() {
        TransactionDTO dto = request(null, "222", 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
        verifyNoInteractions(customerRepository, transactionRepository);
    }

    @Test
    void transferMoneyRejectsNullReceiver() {
        TransactionDTO dto = request("111", null, 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.transferMoney(dto));
    }

    @Test
    void transferMoneyRejectsUnknownSender() {
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.empty());
        TransactionDTO dto = request("111", "222", 10.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(dto));
        assertEquals("Sender Account Number not found", ex.getMessage());
    }

    @Test
    void transferMoneyRejectsUnknownReceiver() {
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(customer("111", 100.0)));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.empty());
        TransactionDTO dto = request("111", "222", 10.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(dto));
        assertEquals("Receiver Account Number not found", ex.getMessage());
    }

    @Test
    void transferMoneyRejectsInsufficientBalance() {
        givenAccounts(customer("111", 10.0), customer("222", 0.0));
        TransactionDTO dto = request("111", "222", 50.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(dto));
        assertEquals("Sender Balance not enough", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    // ---------- consultas ----------

    @Test
    void getTransactionsForAccountReturnsSentAndReceived() {
        when(transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber("111", "111"))
                .thenReturn(List.of(transaction(1L, "111", "222", 10.0), transaction(2L, "333", "111", 5.0)));

        List<TransactionDTO> result = transactionService.getTransactionsForAccount("111");

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("333", result.get(1).getSenderAccountNumber());
    }

    @Test
    void getAllTransactionsReturnsMappedList() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction(1L, "111", "222", 10.0)));

        List<TransactionDTO> result = transactionService.getAlltransactions();

        assertEquals(1, result.size());
        assertEquals(10.0, result.get(0).getAmount());
    }

    @Test
    void getTransactionByIdReturnsTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 10.0)));

        assertEquals("222", transactionService.getTransactionById(1L).getReceiverAccountNumber());
    }

    @Test
    void getTransactionByIdThrowsWhenNotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(1L));
    }

    // ---------- updateTransactionById ----------

    @Test
    void updateTransactionWithSmallerAmountRefundsSender() {
        Customer sender = customer("111", 0.0);
        Customer receiver = customer("222", 100.0);
        givenAccounts(sender, receiver);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 100.0)));

        TransactionDTO result = transactionService.updateTransactionById(1L, request("111", "222", 60.0));

        assertEquals(40.0, sender.getBalance());
        assertEquals(60.0, receiver.getBalance());
        assertEquals(1L, result.getId());
        assertEquals(60.0, result.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransactionWithBiggerAmountChargesSender() {
        Customer sender = customer("111", 100.0);
        Customer receiver = customer("222", 0.0);
        givenAccounts(sender, receiver);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 50.0)));

        transactionService.updateTransactionById(1L, request("111", "222", 80.0));

        assertEquals(70.0, sender.getBalance());
        assertEquals(30.0, receiver.getBalance());
    }

    @Test
    void updateTransactionThrowsWhenNotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());
        TransactionDTO dto = request("111", "222", 10.0);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransactionById(1L, dto));
    }

    @Test
    void updateTransactionRejectsUnknownSender() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 10.0)));
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.empty());
        TransactionDTO dto = request("111", "222", 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.updateTransactionById(1L, dto));
    }

    @Test
    void updateTransactionRejectsUnknownReceiver() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 10.0)));
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(customer("111", 10.0)));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.empty());
        TransactionDTO dto = request("111", "222", 10.0);

        assertThrows(IllegalArgumentException.class, () -> transactionService.updateTransactionById(1L, dto));
    }

    @Test
    void updateTransactionRejectsDifferentSender() {
        givenAccounts(customer("333", 10.0), customer("222", 10.0));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 10.0)));
        TransactionDTO dto = request("333", "222", 10.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.updateTransactionById(1L, dto));
        assertEquals("The sender's account does not match", ex.getMessage());
    }

    @Test
    void updateTransactionRejectsDifferentReceiver() {
        givenAccounts(customer("111", 10.0), customer("333", 10.0));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 10.0)));
        TransactionDTO dto = request("111", "333", 10.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.updateTransactionById(1L, dto));
        assertEquals("The receiver's account does not match", ex.getMessage());
    }

    // ---------- deleteTransactionById ----------

    @Test
    void deleteTransactionRevertsBalances() {
        Customer sender = customer("111", 70.0);
        Customer receiver = customer("222", 30.0);
        givenAccounts(sender, receiver);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 30.0)));

        String result = transactionService.deleteTransactionById(1L);

        assertEquals(100.0, sender.getBalance());
        assertEquals(0.0, receiver.getBalance());
        assertEquals("Transaction has been deleted", result);
        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void deleteTransactionThrowsWhenNotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransactionById(1L));
    }

    @Test
    void deleteTransactionThrowsWhenSenderMissing() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 30.0)));
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransactionById(1L));
    }

    @Test
    void deleteTransactionThrowsWhenReceiverMissing() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 30.0)));
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(customer("111", 0.0)));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransactionById(1L));
    }

    @Test
    void deleteTransactionRejectsWhenReceiverHasNoFunds() {
        givenAccounts(customer("111", 0.0), customer("222", 10.0));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction(1L, "111", "222", 30.0)));

        assertThrows(IllegalArgumentException.class, () -> transactionService.deleteTransactionById(1L));
        verify(transactionRepository, never()).deleteById(any());
    }
}
