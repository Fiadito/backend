package pe.edu.upc.fiadito.platform.credit.domain.model.commands;

import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleRequest;

public record RegisterSaleCommand(Long businessId, SaleRequest request) {
}
