package com.appsdeveloperblog.ws.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.appsdeveloperblog.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.ws.io.entity.AddressEntity;
import com.appsdeveloperblog.ws.io.entity.UserEntity;
import com.appsdeveloperblog.ws.io.repository.UserRepository;
import com.appsdeveloperblog.ws.shared.AmazonSES;
import com.appsdeveloperblog.ws.shared.Utils;
import com.appsdeveloperblog.ws.shared.dto.AddressDTO;
import com.appsdeveloperblog.ws.shared.dto.UserDto;

class UserServiceImplTest {

	@InjectMocks
	UserServiceImpl userService;

	@Mock
	UserRepository userRepository;

	@Mock
	Utils utils;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Mock
	AmazonSES amazonSES;

	UserEntity userEntity;

	String userId = "wertr234345";

	String encryptedPassword = "2345543sr3423";

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setFirstName("Soresa");
		userEntity.setLastName("Hailu");
		userEntity.setUserId(userId);
		userEntity.setEncryptedPassword(encryptedPassword);
		userEntity.setEmail("test@test.com");
		userEntity.setEmailVerificationToken("8765435678");
		userEntity.setAddresses(getAddressesEntity());

	}

	@Test
	void testGetUser() {
		when(userRepository.findUserByEmail(anyString())).thenReturn(userEntity);

		UserDto userDto = userService.getUser("test@test.com");

		assertNotNull(userDto);
		assertEquals("Soresa", userDto.getFirstName());

	}

	@Test
	void testGetUser_UsernameNotFoundException() {
		when(userRepository.findUserByEmail(anyString())).thenReturn(null);

		assertThrows(UsernameNotFoundException.class, () -> {
			UserDto userDto = userService.getUser("test@test.com");
		});
	}

	@Test
	void testCreateUser() {

		when(userRepository.findUserByEmail(anyString())).thenReturn(null);
		when(utils.generateAddressId(anyInt())).thenReturn("2345432");
		when(utils.generateUserId(anyInt())).thenReturn(userId);
		when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptedPassword);
		when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

		Mockito.doNothing().when(amazonSES).verifyEmail(any(UserDto.class));

		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstName("Soresa");
		userDto.setLastName("Hailu");
		userDto.setPassword("1234523");
		userDto.setEmail("test@test.com");

		UserDto storedUserDetails = userService.createUser(userDto);
		assertNotNull(storedUserDetails);
		assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
		assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
		assertNotNull(storedUserDetails.getUserId());

		assertEquals(storedUserDetails.getAddresses().size(), userEntity.getAddresses().size());

		verify(utils, times(2)).generateAddressId(30);
		verify(bCryptPasswordEncoder, times(1)).encode("1234523");
		verify(userRepository, times(1)).save(any(UserEntity.class));
	}

	private List<AddressDTO> getAddressesDto() {

		AddressDTO addressDto = new AddressDTO();
		addressDto.setType("shipping");
		addressDto.setCity("Addis Ababa");
		addressDto.setCountry("Ethiopia");
		addressDto.setPostalCode("Abc124");
		addressDto.setStreetName("123 Street name");

		AddressDTO billingAddressDto = new AddressDTO();
		billingAddressDto.setType("billing");
		billingAddressDto.setCity("Iteya");
		billingAddressDto.setCountry("Ethiopia");
		billingAddressDto.setPostalCode("ABC123");
		billingAddressDto.setStreetName("123 Street name");

		List<AddressDTO> addresses = new ArrayList<>();
		addresses.add(addressDto);
		addresses.add(billingAddressDto);

		return addresses;
	}

	private List<AddressEntity> getAddressesEntity() {

		List<AddressDTO> addresses = getAddressesDto();

		Type listType = new TypeToken<List<AddressEntity>>() {
		}.getType();

		return new ModelMapper().map(addresses, listType);
	}

	@Test
	void testCreateUser_UserServiceException() {
		when(userRepository.findUserByEmail(anyString())).thenReturn(userEntity);

		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstName("Soresa");
		userDto.setLastName("Hailu");
		userDto.setPassword("1234523");
		userDto.setEmail("test@test.com");

		assertThrows(UserServiceException.class, () -> {
			userService.createUser(userDto);
		});

	}

}
