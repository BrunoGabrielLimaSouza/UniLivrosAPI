package com.unilivros.repository;

import com.unilivros.model.Troca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrocaRepository extends JpaRepository<Troca, Long> {
    
    List<Troca> findByStatus(Troca.StatusTroca status);
    
    @Query("SELECT t FROM Troca t WHERE t.agendamento.usuario = :usuario")
    List<Troca> findByUsuario(@Param("usuario") com.unilivros.model.Usuario usuario);
    
    @Query("SELECT t FROM Troca t WHERE t.agendamento.usuario = :usuario AND t.status = :status")
    List<Troca> findByUsuarioAndStatus(@Param("usuario") com.unilivros.model.Usuario usuario, 
                                      @Param("status") Troca.StatusTroca status);
    
    @Query("SELECT t FROM Troca t WHERE t.dataConfirmacao BETWEEN :inicio AND :fim")
    List<Troca> findByDataConfirmacaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT t FROM Troca t WHERE t.avaliacao >= :avaliacaoMinima")
    List<Troca> findByAvaliacaoGreaterThanEqual(@Param("avaliacaoMinima") Double avaliacaoMinima);
    
    @Query("SELECT COUNT(t) FROM Troca t WHERE t.status = :status")
    Long countByStatus(@Param("status") Troca.StatusTroca status);
    
    @Query("SELECT AVG(t.avaliacao) FROM Troca t WHERE t.avaliacao IS NOT NULL")
    Double findAverageAvaliacao();
}
