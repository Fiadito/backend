package pe.edu.upc.fiadito.platform.audit.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.fiadito.platform.audit.domain.model.aggregates.OperationLog;

import java.util.List;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    List<OperationLog> findTop500ByBusinessIdOrderByIdDesc(Long businessId);

    List<OperationLog> findTop500ByBusinessIdAndActionOrderByIdDesc(Long businessId, String action);
}
