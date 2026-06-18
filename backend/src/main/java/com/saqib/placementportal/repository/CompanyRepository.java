package com.saqib.placementportal.repository;

import com.saqib.placementportal.entity.Company;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUserId(Long userId);

    Optional<Company> findByUserEmailIgnoreCase(String email);

    long countByApprovedFalse();

    Page<Company> findByApprovedFalse(Pageable pageable);

    @Query("""
            select c from Company c
            where (:approved is null or c.approved = :approved)
            and (
                :query is null
                or lower(c.companyName) like lower(concat('%', :query, '%'))
                or lower(c.user.email) like lower(concat('%', :query, '%'))
                or lower(c.location) like lower(concat('%', :query, '%'))
            )
            """)
    Page<Company> search(@Param("query") String query, @Param("approved") Boolean approved, Pageable pageable);
}
