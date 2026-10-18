package pe.edu.upc.fiadito.platform.catalog.domain.services;

import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;
import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.ChangeProductStatusCommand;
import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.SaveProductCommand;

public interface ProductCommandService {
    Product handle(SaveProductCommand command, Long actorUserId, String actorName);

    Product handle(ChangeProductStatusCommand command, Long actorUserId, String actorName);
}
