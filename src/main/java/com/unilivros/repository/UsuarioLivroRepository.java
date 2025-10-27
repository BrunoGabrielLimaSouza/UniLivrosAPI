package com.unilivros.repository;

import com.unilivros.model.Livro;
import com.unilivros.model.Usuario;
import com.unilivros.model.UsuarioLivro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioLivroRepository extends JpaRepository<UsuarioLivro, Long> {
    
    List<UsuarioLivro> findByUsuario(Usuario usuario);
    
    List<UsuarioLivro> findByLivro(Livro livro);
    
    List<UsuarioLivro> findByUsuarioAndDisponivelParaTroca(Usuario usuario, Boolean disponivelParaTroca);
    
    Optional<UsuarioLivro> findByUsuarioAndLivro(Usuario usuario, Livro livro);
    
    boolean existsByUsuarioAndLivro(Usuario usuario, Livro livro);
    
    @Query("SELECT ul FROM UsuarioLivro ul WHERE ul.usuario = :usuario AND ul.disponivelParaTroca = true")
    List<UsuarioLivro> findDisponiveisParaTrocaByUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT ul FROM UsuarioLivro ul WHERE ul.livro = :livro AND ul.disponivelParaTroca = true")
    List<UsuarioLivro> findDisponiveisParaTrocaByLivro(@Param("livro") Livro livro);
    
    @Query("SELECT ul FROM UsuarioLivro ul WHERE ul.usuario != :usuario AND ul.disponivelParaTroca = true")
    List<UsuarioLivro> findDisponiveisParaTrocaExcludingUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT COUNT(ul) FROM UsuarioLivro ul WHERE ul.usuario = :usuario")
    Long countByUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT COUNT(ul) FROM UsuarioLivro ul WHERE ul.usuario = :usuario AND ul.disponivelParaTroca = true")
    Long countDisponiveisParaTrocaByUsuario(@Param("usuario") Usuario usuario);
}
