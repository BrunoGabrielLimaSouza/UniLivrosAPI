package com.unilivros.repository;

import com.unilivros.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {
    
    Optional<Livro> findByIsbn(String isbn);
    
    boolean existsByIsbn(String isbn);
    
    @Query("SELECT l FROM Livro l WHERE l.titulo LIKE %:titulo%")
    List<Livro> findByTituloContaining(@Param("titulo") String titulo);
    
    @Query("SELECT l FROM Livro l WHERE l.autor LIKE %:autor%")
    List<Livro> findByAutorContaining(@Param("autor") String autor);
    
    @Query("SELECT l FROM Livro l WHERE l.genero = :genero")
    List<Livro> findByGenero(@Param("genero") String genero);
    
    @Query("SELECT l FROM Livro l WHERE l.editora = :editora")
    List<Livro> findByEditora(@Param("editora") String editora);
    
    @Query("SELECT l FROM Livro l WHERE l.ano = :ano")
    List<Livro> findByAno(@Param("ano") Integer ano);
    
    @Query("SELECT l FROM Livro l WHERE l.condicao = :condicao")
    List<Livro> findByCondicao(@Param("condicao") Livro.CondicaoLivro condicao);
    
    @Query("SELECT l FROM Livro l WHERE l.titulo LIKE %:termo% OR l.autor LIKE %:termo% OR l.genero LIKE %:termo%")
    List<Livro> findByTituloContainingOrAutorContainingOrGeneroContaining(@Param("termo") String termo);
    
    @Query("SELECT l FROM Livro l WHERE l.ano BETWEEN :anoInicio AND :anoFim")
    List<Livro> findByAnoBetween(@Param("anoInicio") Integer anoInicio, @Param("anoFim") Integer anoFim);
    
    @Query("SELECT DISTINCT l.genero FROM Livro l ORDER BY l.genero")
    List<String> findDistinctGeneros();
    
    @Query("SELECT DISTINCT l.editora FROM Livro l ORDER BY l.editora")
    List<String> findDistinctEditoras();

    Page<Livro> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT l FROM Livro l WHERE l. googleId = :googleId")
    List<Livro> findByGoogleId(@Param("googleId") String googleId);
}
