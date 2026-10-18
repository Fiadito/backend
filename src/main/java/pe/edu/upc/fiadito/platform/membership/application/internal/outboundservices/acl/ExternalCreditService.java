package pe.edu.upc.fiadito.platform.membership.application.internal.outboundservices.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upc.fiadito.platform.credit.interfaces.acl.CreditContextFacade;

import java.math.BigDecimal;

/**
 * Servicio de salida hacia el contexto Credit: deuda pendiente de un socio (para darlo de baja).
 */
@Service
@RequiredArgsConstructor
public class ExternalCreditService {

    private final CreditContextFacade creditContextFacade;

    public BigDecimal pendingDebtOf(Long memberId) {
        return creditContextFacade.pendingCapitalOf(memberId);
    }
}
