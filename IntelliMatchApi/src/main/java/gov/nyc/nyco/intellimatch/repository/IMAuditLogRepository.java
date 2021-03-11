package gov.nyc.nyco.intellimatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gov.nyc.nyco.intellimatch.models.IMAuditLog;

public interface IMAuditLogRepository extends JpaRepository<IMAuditLog,Integer> {

}
