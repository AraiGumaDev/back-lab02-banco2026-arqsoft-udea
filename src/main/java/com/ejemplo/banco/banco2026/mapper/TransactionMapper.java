package com.ejemplo.banco.banco2026.mapper;

import com.ejemplo.banco.banco2026.dto.TransactionDTO;
import com.ejemplo.banco.banco2026.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE= Mappers.getMapper(TransactionMapper.class);
    TransactionDTO toDTO(Transaction transaction);
    Transaction toEntity(TransactionDTO transactionDTO);
}