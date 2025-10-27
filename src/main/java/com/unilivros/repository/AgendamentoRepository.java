package com.unilivros.repository;

import com.unilivros.model.Agendamento;
import com.unilivros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    List<Agendamento> findByUsuario(Usuario usuario);
    
    List<Agendamento> findByStatus(Agendamento.StatusAgendamento status);
    
    List<Agendamento> findByUsuarioAndStatus(Usuario usuario, Agendamento.StatusAgendamento status);
    
    @Query("SELECT a FROM Agendamento a WHERE a.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findByDataHoraBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT a FROM Agendamento a WHERE a.usuario = :usuario AND a.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findByUsuarioAndDataHoraBetween(@Param("usuario") Usuario usuario, 
                                                     @Param("inicio") LocalDateTime inicio, 
                                                     @Param("fim") LocalDateTime fim);
    
    @Query("SELECT a FROM Agendamento a WHERE a.dataHora >= :dataAtual AND a.status = :status")
    List<Agendamento> findFuturosByStatus(@Param("dataAtual") LocalDateTime dataAtual, 
                                         @Param("status") Agendamento.StatusAgendamento status);
    
    @Query("SELECT a FROM Agendamento a WHERE a.dataHora < :dataAtual AND a.status = :status")
    List<Agendamento> findPassadosByStatus(@Param("dataAtual") LocalDateTime dataAtual, 
                                          @Param("status") Agendamento.StatusAgendamento status);
}
