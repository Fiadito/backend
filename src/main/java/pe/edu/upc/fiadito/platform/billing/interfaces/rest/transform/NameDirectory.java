package pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.upc.fiadito.platform.catalog.interfaces.acl.CatalogContextFacade;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;

import java.util.Map;
import java.util.stream.Collectors;

/** Nombres de socios y productos del negocio para armar las respuestas. */
@Component
@RequiredArgsConstructor
public class NameDirectory {

    private final MembershipContextFacade membership;
    private final CatalogContextFacade catalog;

    public Map<Long, String> memberNames(Long businessId) {
        return membership.membersOf(businessId).stream().collect(Collectors.toMap(Member::getId, Member::getFullName));
    }

    public Map<Long, String> productNames(Long businessId) {
        return catalog.productNames(businessId);
    }
}
