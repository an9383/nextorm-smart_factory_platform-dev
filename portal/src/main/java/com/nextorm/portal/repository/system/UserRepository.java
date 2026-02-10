package com.nextorm.portal.repository.system;

import com.nextorm.portal.entity.system.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	@Query("""
		SELECT DISTINCT u FROM User u
		  LEFT JOIN FETCH u.userRoles ur
		  LEFT JOIN FETCH u.userSetting us
		  LEFT JOIN FETCH ur.role
		 WHERE u.loginId = :loginId
		""")
	Optional<User> findByLoginId(@Param("loginId") String loginId);

	@Query("""
		SELECT DISTINCT u FROM User u
		  LEFT JOIN FETCH u.userRoles ur
		  LEFT JOIN FETCH ur.role
		  ORDER BY :#{#sort}
		""")
	List<User> findAll(Sort sort);

	@Query("""
		SELECT DISTINCT u FROM User u
		  LEFT JOIN FETCH u.userRoles ur
		  LEFT JOIN FETCH ur.role
		 WHERE u.id = :userId
		""")
	Optional<User> findById(@Param("userId") Long userId);

	@Query("""
		SELECT DISTINCT u FROM User u
		  LEFT JOIN FETCH u.userRoles ur
		  LEFT JOIN FETCH ur.role
		  WHERE u.id IN :userIds
		""")
	List<User> findAllById(@Param("userIds") List<Long> userIds);

	List<User> findByUserRolesRoleId(@Param("roleId") Long roleId);

	List<User> findByIdIn(List<Long> userIds);
}
