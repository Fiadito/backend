package pe.edu.upc.fiadito.platform.membership.interfaces.rest.transform;

import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources.MemberResource;

import java.math.BigDecimal;

public class MemberResourceAssembler {
    public static MemberResource toResource(Member m, String email, BigDecimal debt, BigDecimal available, boolean overdue) {
        return new MemberResource(m.getId(), m.getBusinessId(), m.getDni(), m.getFullName(), m.getPhone(), m.getAddress(),
                m.getCurrency().name(), m.getCreditLimit(), m.getMaxInstallments(), m.getStatus().name(), email, debt, available, overdue);
    }
}
