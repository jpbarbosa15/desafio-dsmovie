package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;
	@Mock
	private UserRepository repository;
	@Mock
	private CustomUserUtil customUserUtil;
	private String existingUsername,nonExistingUsername;
	private UserEntity user;
	private List<UserDetailsProjection> userDetailsProjection;

	@BeforeEach
	void setUp() {
		existingUsername = "maria@gmail.com";
		nonExistingUsername = "user@gmail.com";
		user = UserFactory.createUserEntity();
		userDetailsProjection = UserDetailsFactory.createCustomAdminUser(existingUsername);

		when(repository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userDetailsProjection);
		when(repository.searchUserAndRolesByUsername(nonExistingUsername)).thenReturn(new ArrayList<>());

		when(repository.findByUsername(existingUsername)).thenReturn(Optional.of(user));
		when(repository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());
	}

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {
		when(customUserUtil.getLoggedUsername()).thenReturn(existingUsername);

		UserEntity result = service.authenticated();

		assertNotNull(result);
		assertEquals(existingUsername,result.getUsername());
	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		doThrow(ClassCastException.class).when(customUserUtil).getLoggedUsername();

		assertThrows(UsernameNotFoundException.class,()->{
			UserEntity result = service.authenticated();
		});


	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
		UserDetails result = service.loadUserByUsername(existingUsername);

		assertNotNull(result);
		assertEquals(existingUsername,result.getUsername());
	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		assertThrows(UsernameNotFoundException.class,()->{
			UserDetails result = service.loadUserByUsername(nonExistingUsername);
		});

	}
}
