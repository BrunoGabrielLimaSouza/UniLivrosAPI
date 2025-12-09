package com.unilivros.repository;

import com.unilivros.model.Proposta;
import com.unilivros.model.Troca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrocaRepository extends JpaRepository<Troca, Long> {

    List<Troca> findByStatus(Troca.StatusTroca status);

    // ✅ Buscar por proposta
    boolean existsByProposta(Proposta proposta);

    // ✅ Buscar por QR Code
    Optional<Troca> findByQrCode(String qrCode);

    @Query("SELECT t FROM Troca t JOIN t.usuarios tu WHERE tu.usuario.id = :usuarioId")
    List<Troca> findByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t FROM Troca t JOIN t.usuarios tu WHERE tu.usuario.id = :usuarioId AND t.status = :status")
    List<Troca> findByUsuarioAndStatus(@Param("usuarioId") Long usuarioId,
                                       @Param("status") Troca.StatusTroca status);

    @Query("SELECT t FROM Troca t WHERE t.dataConfirmacao BETWEEN :inicio AND :fim")
    List<Troca> findByDataConfirmacaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT t FROM Troca t WHERE t.avaliacao >= :avaliacaoMinima")
    List<Troca> findByAvaliacaoGreaterThanEqual(@Param("avaliacaoMinima") Double avaliacaoMinima);

    @Query("SELECT COUNT(t) FROM Troca t WHERE t.status = :status")
    Long countByStatus(@Param("status") Troca.StatusTroca status);

    @Query("SELECT AVG(t.avaliacao) FROM Troca t WHERE t.avaliacao IS NOT NULL")
    Double findAverageAvaliacao();

    // ➕ Conta quantas trocas um usuário concluiu com sucesso
    @Query("SELECT COUNT(t) FROM Troca t JOIN t.usuarios tu WHERE tu.usuario.id = :usuarioId AND t.status = 'CONCLUIDA'")
    Long countConcluidasByUsuario(@Param("usuarioId") Long usuarioId);

    // ➕ Calcula a média de avaliações de todas as trocas de um usuário
    @Query("SELECT AVG(t.avaliacao) FROM Troca t JOIN t.usuarios tu WHERE tu.usuario.id = :usuarioId AND t.avaliacao IS NOT NULL AND t.status = 'CONCLUIDA'")
    Double findAverageAvaliacaoByUsuario(@Param("usuarioId") Long usuarioId);
}