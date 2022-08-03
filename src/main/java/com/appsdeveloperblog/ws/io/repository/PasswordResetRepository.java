package com.appsdeveloperblog.ws.io.repository;

import org.springframework.data.repository.CrudRepository;

import com.appsdeveloperblog.ws.io.entity.PasswordResetTokenEntity;

public interface PasswordResetRepository extends CrudRepository<PasswordResetTokenEntity, Long>{

	PasswordResetTokenEntity findByToken(String token);

}
