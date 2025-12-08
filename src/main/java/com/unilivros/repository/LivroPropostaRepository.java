package com.unilivros.repository;

import com.unilivros.model. LivroProposta;
import com.unilivros.model. Proposta;
import org.springframework.data.jpa.repository. JpaRepository;
import org. springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroPropostaRepository extends JpaRepository<LivroProposta, Long> {
    List<LivroProposta> findByProposta(Proposta proposta);
}