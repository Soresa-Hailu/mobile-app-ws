package com.appsdeveloperblog.ws.io.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.appsdeveloperblog.ws.io.entity.UserEntity;

@Repository
//public interface UserRepository extends CrudRepository <UserEntity, Long>
public interface UserRepository extends PagingAndSortingRepository<UserEntity, Long> {

	UserEntity findByUserId(String userId);

	UserEntity findUserByEmailVerificationToken(String token);

	UserEntity findUserByEmail(String email);

}
