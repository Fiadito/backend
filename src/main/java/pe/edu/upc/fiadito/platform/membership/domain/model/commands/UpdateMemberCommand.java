package pe.edu.upc.fiadito.platform.membership.domain.model.commands;

import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.Currency;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;

public record UpdateMemberCommand(Long businessId, Long memberId, String dni, String fullName, String phone, String address,
                                  Currency currency, RatePeriod lateRatePeriod, BigDecimal lateRateValue, BigDecimal creditLimit,
                                  Integer maxInstallments) {
}
