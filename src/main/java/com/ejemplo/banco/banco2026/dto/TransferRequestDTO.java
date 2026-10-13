package com.ejemplo.banco.banco2026.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDTO {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private Double amount;
    public TransferRequestDTO() {
    }

}
