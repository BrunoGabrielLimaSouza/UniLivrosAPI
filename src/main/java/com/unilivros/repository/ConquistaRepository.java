package com.unilivros.repository;

import com.unilivros.model.Conquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConquistaRepository extends JpaRepository<Conquista, Long> {
    
    List<Conquista> findByTipo(Conquista.TipoConquista tipo);
    
    @Query("SELECT c FROM Conquista c WHERE c.xpNecessario <= :xp ORDER BY c.xpNecessario DESC")
    List<Conquista> findByXpNecessarioLessThanEqualOrderByXpNecessarioDesc(@Param("xp") Integer xp);
    
    @Query("SELECT c FROM Conquista c WHERE c.xpNecessario > :xp ORDER BY c.xpNecessario ASC")
    List<Conquista> findByXpNecessarioGreaterThanOrderByXpNecessarioAsc(@Param("xp") Integer xp);
    
    @Query("SELECT c FROM Conquista c WHERE c.nome LIKE %:nome%")
    List<Conquista> findByNomeContaining(@Param("nome") String nome);
}
