package com.unilivros.repository;

import com.unilivros.model.Proposta;
import com.unilivros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, Long> {
    
    List<Proposta> findByProponente(Usuario proponente);
    
    List<Proposta> findByProposto(Usuario proposto);
    
    List<Proposta> findByProponenteAndStatus(Usuario proponente, Proposta.StatusProposta status);
    
    List<Proposta> findByPropostoAndStatus(Usuario proposto, Proposta.StatusProposta status);
    
    @Query("SELECT p FROM Proposta p WHERE (p.proponente = :usuario OR p.proposto = :usuario) AND p.status = :status")
    List<Proposta> findByUsuarioAndStatus(@Param("usuario") Usuario usuario, @Param("status") Proposta.StatusProposta status);
    
    @Query("SELECT p FROM Proposta p WHERE p.proponente = :usuario OR p.proposto = :usuario")
    List<Proposta> findByUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT p FROM Proposta p WHERE p.status = :status")
    List<Proposta> findByStatus(@Param("status") Proposta.StatusProposta status);
    
    @Query("SELECT COUNT(p) FROM Proposta p WHERE p.proponente = :usuario AND p.status = :status")
    Long countByProponenteAndStatus(@Param("usuario") Usuario usuario, @Param("status") Proposta.StatusProposta status);
    
    @Query("SELECT COUNT(p) FROM Proposta p WHERE p.proposto = :usuario AND p.status = :status")
    Long countByPropostoAndStatus(@Param("usuario") Usuario usuario, @Param("status") Proposta.StatusProposta status);

    List<Proposta> findByProponente_Id(Long id);

    List<Proposta> findByProposto_Id(Long id);

}
