package gov.nyc.nyco.intellimatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gov.nyc.nyco.intellimatch.models.IMAccessToken;

@Repository
public interface IMAccessTokenRepository extends JpaRepository<IMAccessToken,Integer> {
	List<IMAccessToken> findAllByOrgAgncyName(String orgAgncyName);

}
