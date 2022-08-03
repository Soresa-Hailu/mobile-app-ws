package com.appsdeveloperblog.ws.service;

import java.util.List;

import com.appsdeveloperblog.ws.shared.dto.AddressDTO;

public interface AddressService {

	List<AddressDTO> getAddresses(String userId);

	AddressDTO getAddress(String addressId);
}
