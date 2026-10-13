package com.ejemplo.banco.banco2026.mapper;


import com.ejemplo.banco.banco2026.dto.CustomerDTO;
import com.ejemplo.banco.banco2026.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    CustomerDTO toDTO(Customer customer);
    Customer toEntity(CustomerDTO customerDTO);
}
