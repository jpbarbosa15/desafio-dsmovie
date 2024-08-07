package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;
	@Mock
	private MovieRepository repository;
	private String movieTitle;
	private PageImpl<MovieEntity> page;
	private MovieEntity movieEntity;
	private Long existingId, nonExistingId, dependentId;
	private MovieDTO movieDTO;


	@BeforeEach
	void setUp() {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		movieTitle = "Filme Teste";
		movieEntity = MovieFactory.createMovieEntity(movieTitle);
		movieDTO = MovieFactory.createMovieDTO();
		page = new PageImpl<>(List.of(movieEntity));

		when(repository.searchByTitle(any(),(Pageable) any())).thenReturn(page);

		when(repository.findById(existingId)).thenReturn(Optional.of(movieEntity));
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		when(repository.save(any())).thenReturn(movieEntity);

		when(repository.getReferenceById(existingId)).thenReturn(movieEntity);
		when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		when(repository.existsById(existingId)).thenReturn(true);
		when(repository.existsById(nonExistingId)).thenReturn(false);
		when(repository.existsById(dependentId)).thenReturn(true);

		doNothing().when(repository).deleteById(existingId);
		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}

	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		Pageable pageable = PageRequest.of(0,12);
		Page<MovieDTO> result = service.findAll(movieTitle,pageable);

		assertNotNull(result);
		assertEquals(1,result.getSize());
		assertEquals(movieTitle,result.iterator().next().getTitle());
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.findById(existingId);

		assertNotNull(result);
		assertEquals(movieTitle,result.getTitle());
		assertEquals(existingId,result.getId());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class,() ->{
			MovieDTO result = service.findById(nonExistingId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = service.insert(movieDTO);

		assertNotNull(result);
		assertEquals(movieEntity.getTitle(),result.getTitle());
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.update(existingId,movieDTO);

		assertEquals(existingId,result.getId());
		assertEquals(movieDTO.getTitle(),result.getTitle());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class,() ->{
			MovieDTO result = service.update(nonExistingId,movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		assertDoesNotThrow(() ->{
			service.delete(existingId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class,() ->{
			service.delete(nonExistingId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		assertThrows(DatabaseException.class,() ->{
			service.delete(dependentId);
		});
	}
}
