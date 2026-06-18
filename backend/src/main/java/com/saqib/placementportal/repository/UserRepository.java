package com.saqib.placementportal.repository;

import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select distinct u from UserAccount u
            join u.roles r
            where (:role is null or r.name = :role)
            and (
                :query is null
                or lower(u.fullName) like lower(concat('%', :query, '%'))
                or lower(u.email) like lower(concat('%', :query, '%'))
            )
            """)
    Page<UserAccount> search(@Param("query") String query, @Param("role") RoleName role, Pageable pageable);
}
