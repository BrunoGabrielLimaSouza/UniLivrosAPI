package com.unilivros.repository;

import com.unilivros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByMatricula(String matricula);
    
    boolean existsByEmail(String email);
    
    boolean existsByMatricula(String matricula);
    
    @Query("SELECT u FROM Usuario u WHERE u.curso = :curso")
    List<Usuario> findByCurso(@Param("curso") String curso);
    
    @Query("SELECT u FROM Usuario u WHERE u.semestre = :semestre")
    List<Usuario> findBySemestre(@Param("semestre") Integer semestre);
    
    @Query("SELECT u FROM Usuario u WHERE u.curso = :curso AND u.semestre = :semestre")
    List<Usuario> findByCursoAndSemestre(@Param("curso") String curso, @Param("semestre") Integer semestre);
    
    @Query("SELECT u FROM Usuario u WHERE u.xp >= :xpMinimo ORDER BY u.xp DESC")
    List<Usuario> findByXpGreaterThanEqualOrderByXpDesc(@Param("xpMinimo") Integer xpMinimo);
    
    @Query("SELECT u FROM Usuario u WHERE u.avaliacao >= :avaliacaoMinima ORDER BY u.avaliacao DESC")
    List<Usuario> findByAvaliacaoGreaterThanEqualOrderByAvaliacaoDesc(@Param("avaliacaoMinima") Double avaliacaoMinima);
    
    @Query("SELECT u FROM Usuario u WHERE u.nome LIKE %:nome% OR u.email LIKE %:email%")
    List<Usuario> findByNomeContainingOrEmailContaining(@Param("nome") String nome, @Param("email") String email);
}
