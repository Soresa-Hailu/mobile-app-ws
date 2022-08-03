package com.appsdeveloperblog.ws.io.repository;

import com.appsdeveloperblog.ws.io.entity.UserEntity;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.appsdeveloperblog.ws.io.entity.AddressEntity;

@Repository
public interface AddressRepository extends CrudRepository<AddressEntity, Long> {

	public List<AddressEntity> findAllByUserDetails(UserEntity userEntity);

	public AddressEntity findByAddressId(String addressId);

}
