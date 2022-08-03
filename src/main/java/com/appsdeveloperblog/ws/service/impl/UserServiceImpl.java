package com.appsdeveloperblog.ws.service.impl;

import java.util.*;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appsdeveloperblog.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.ws.io.entity.PasswordResetTokenEntity;
import com.appsdeveloperblog.ws.io.entity.UserEntity;
import com.appsdeveloperblog.ws.io.repository.UserRepository;
import com.appsdeveloperblog.ws.io.repository.PasswordResetRepository;
import com.appsdeveloperblog.ws.service.UserService;
import com.appsdeveloperblog.ws.shared.AmazonSES;
import com.appsdeveloperblog.ws.shared.Utils;
import com.appsdeveloperblog.ws.shared.dto.AddressDTO;
import com.appsdeveloperblog.ws.shared.dto.UserDto;
import com.appsdeveloperblog.ws.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordResetRepository passwordResetRepository;
	
	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	AmazonSES amazonSES;

	@Override
	@Transactional
	public UserDto createUser(UserDto userDto) {
		// TODO Auto-generated method stub

		if (userRepository.findUserByEmail(userDto.getEmail()) != null)
			throw new UserServiceException("Record already exists");

		for(int i=0; i<userDto.getAddresses().size(); i++) {
			AddressDTO addressDto = userDto.getAddresses().get(i);
			addressDto.setUserDetails(userDto);
			addressDto.setAddressId(utils.generateAddressId(30));
			userDto.getAddresses().set(i, addressDto);
		}

		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = new UserEntity();
		userEntity = modelMapper.map(userDto, UserEntity.class);
//		BeanUtils.copyProperties(userDto, userEntity);


		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(false);
		
		UserEntity storedUserDetails = userRepository.save(userEntity);

		UserDto returnVal = modelMapper.map(storedUserDetails, UserDto.class);
//		BeanUtils.copyProperties(storedUserDetails, returnVal);
		
//		Send an email messsage to user to verify their email address
		amazonSES.verifyEmail(returnVal);
		return returnVal;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findUserByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),userEntity.getEmailVerificationStatus(),
				true,true,true, new ArrayList<>());

//		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepository.findUserByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto returnValue = new UserDto();
//		ModelMapper modelMapper = new ModelMapper();
//		returnValue = modelMapper.map(userEntity, UserDto.class);

		BeanUtils.copyProperties(userEntity, returnValue);

		return returnValue;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		// TODO Auto-generated method stub

		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException("User with ID: " + userId + " not found");

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto updateUser(String userId, UserDto user) {

		UserDto returnVal = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		UserEntity userEmail = userRepository.findUserByEmail(user.getEmail());
		if (userEmail != null) {
			// check if the user's email is different than the actual user
			if (!userEntity.getUserId().equals(userEmail.getUserId()))
				throw new UserServiceException(ErrorMessages.EMAIL_ADDESS_NOT_VERIFIED.getErrorMessage());
		}
		userEntity.setEmail(user.getEmail());
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());

		UserEntity updatedUserDetails = userRepository.save(userEntity);

		BeanUtils.copyProperties(updatedUserDetails, returnVal);

		return returnVal;
	}

	@Override
	public void deleteUser(String userId) {

		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userRepository.delete(userEntity);

	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {

		List<UserDto> returnValue = new ArrayList<>();

		Pageable pageableRequest = PageRequest.of(page, limit);
		
		Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();

		for(UserEntity useEntity: users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(useEntity, userDto);
			returnValue.add(userDto);
		}
		
		return returnValue;
	}

	@Override
	public boolean verifyEmailToken(String token) {

		boolean returnValue = false;
		
		//Find user by token
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		
		if(userEntity != null) {
			boolean hastokenExpired = Utils.hasTokenExpired(token);
			if(!hastokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}
		return returnValue;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		
		boolean returnValue = false;
		
		UserEntity userEntity = userRepository.findUserByEmail(email);
		
		if(userEntity == null) {
			
			return returnValue;
		}
		
		String token = utils.generatePasswordResetToken(userEntity.getUserId());
		
		PasswordResetTokenEntity passwordResetTokenEntity =  new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		passwordResetRepository.save(passwordResetTokenEntity);
		
		returnValue = new AmazonSES().sendPasswordResetRequest(
				userEntity.getFirstName(),
				userEntity.getEmail(),
				token);
		
		return returnValue;
	}
 
	@Override
	public boolean passwordReset(String token, String password) {
		
		boolean returnValue = false;
		
		if(Utils.hasTokenExpired(token)) {
			return returnValue;
		}
		
		PasswordResetTokenEntity passwordResetTokenEntity = passwordResetRepository.findByToken(token);
		
			if(passwordResetTokenEntity == null) {
				return returnValue;
			}
		
		//Prepare new password
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		
		//update User password in database
		UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
		userEntity.setEncryptedPassword(encodedPassword);
		UserEntity savedUserEntity = userRepository.save(userEntity);
		
		//verify if password was saved successfully
		
		if(savedUserEntity !=null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
			returnValue = true;
		}
		
		//Remove Password Reset token from database
		passwordResetRepository.delete(passwordResetTokenEntity);
		
		return false;
	}

}
