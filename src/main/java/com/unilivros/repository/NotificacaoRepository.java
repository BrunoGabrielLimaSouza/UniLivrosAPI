package com.unilivros. repository;

import com.unilivros.model.Notificacao;
import com.unilivros.model. Usuario;
import org.springframework. data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    List<Notificacao> findByUsuarioAndLidaOrderByCreatedAtDesc(Usuario usuario, Boolean lida);

    Long countByUsuarioAndLida(Usuario usuario, Boolean lida);
}