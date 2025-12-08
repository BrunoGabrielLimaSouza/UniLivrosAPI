package com.unilivros.repository;

import com.unilivros.model. TrocaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrocaUsuarioRepository extends JpaRepository<TrocaUsuario, Long> {
}