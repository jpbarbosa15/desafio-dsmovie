package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;
	@Mock
	private MovieRepository movieRepository;
	@Mock
	private ScoreRepository scoreRepository;
	@Mock
	private UserService userService;

	private MovieEntity movieEntity;
	private UserEntity userEntity;
	private ScoreDTO scoreDTO;
	private ScoreEntity scoreEntity;
	private Long existingMovieId,nonExistingMovieId;

	@BeforeEach
	void setUp() {
		existingMovieId = 1L;
		nonExistingMovieId = 2L;
		scoreDTO = ScoreFactory.createScoreDTO();
		movieEntity = MovieFactory.createMovieEntity();
		userEntity = UserFactory.createUserEntity();
		scoreEntity = ScoreFactory.createScoreEntity();

		when(userService.authenticated()).thenReturn(userEntity);

		when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));
		when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

		when(scoreRepository.saveAndFlush(any())).thenAnswer(invocationOnMock -> {
			movieEntity.getScores().add(scoreEntity);
			return scoreEntity;
		});
		when(movieRepository.save(movieEntity)).thenReturn(movieEntity);
	}

	@Test
	public void saveScoreShouldReturnMovieDTO() {
		MovieDTO result = service.saveScore(scoreDTO);

		assertNotNull(result);
		assertEquals(result.getTitle(),movieEntity.getTitle());
		assertEquals(result.getId(),movieEntity.getId());

	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
		scoreDTO.setMovieId(nonExistingMovieId);

		assertThrows(ResourceNotFoundException.class,() -> {
			MovieDTO result =	service.saveScore(scoreDTO);
		});


	}
}
