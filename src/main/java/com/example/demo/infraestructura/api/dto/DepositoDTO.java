package com.example.demo.infraestructura.api.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DepositoDTO {
	private String numeroCuenta;
    private BigDecimal monto;
}
