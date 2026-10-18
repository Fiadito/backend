package pe.edu.upc.fiadito.platform.credit.domain.services;

import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.RegisterSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.SimulateSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleSimulation;

public interface CreditSaleCommandService {
    SaleSimulation handle(SimulateSaleCommand command);

    CreditSale handle(RegisterSaleCommand command, Long actorUserId, String actorName);
}
