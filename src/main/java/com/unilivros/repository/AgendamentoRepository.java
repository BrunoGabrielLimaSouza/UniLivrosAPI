package com.unilivros.repository;

import com.unilivros.model.Agendamento;
import com.unilivros.model. Proposta;
import org.springframework. data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository. Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype. Repository;

import java.time. LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // ✅ Buscar por status
    List<Agendamento> findByStatus(Agendamento.StatusAgendamento status);

    // ✅ Buscar por proposta
    Optional<Agendamento> findByProposta(Proposta proposta);

    // ✅ Buscar agendamentos de um usuário (através da proposta)
    @Query("SELECT a FROM Agendamento a WHERE a.proposta. proponente.id = :usuarioId OR a.proposta.proposto.id = :usuarioId")
    List<Agendamento> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    // ✅ Buscar agendamentos de um usuário com status específico
    @Query("SELECT a FROM Agendamento a WHERE (a.proposta.proponente.id = :usuarioId OR a.proposta.proposto. id = :usuarioId) AND a.status = :status")
    List<Agendamento> findByUsuarioIdAndStatus(@Param("usuarioId") Long usuarioId, @Param("status") Agendamento.StatusAgendamento status);

    // ✅ Buscar agendamentos em um período (pela data da proposta)
    @Query("SELECT a FROM Agendamento a WHERE a.proposta.dataHoraSugerida BETWEEN :inicio AND :fim")
    List<Agendamento> findByDataHoraBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // ✅ Buscar agendamentos futuros com status
    @Query("SELECT a FROM Agendamento a WHERE a.proposta.dataHoraSugerida >= :dataAtual AND a.status = :status")
    List<Agendamento> findFuturosByStatus(@Param("dataAtual") LocalDateTime dataAtual, @Param("status") Agendamento.StatusAgendamento status);

    // ✅ Buscar agendamentos passados com status
    @Query("SELECT a FROM Agendamento a WHERE a.proposta.dataHoraSugerida < :dataAtual AND a.status = :status")
    List<Agendamento> findPassadosByStatus(@Param("dataAtual") LocalDateTime dataAtual, @Param("status") Agendamento.StatusAgendamento status);

    // ✅ Contar agendamentos por status
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.status = :status")
    Long countByStatus(@Param("status") Agendamento.StatusAgendamento status);

    // ✅ Verificar se existe agendamento para uma proposta
    boolean existsByProposta(Proposta proposta);
}