package com.unilivros.service;

import com.unilivros.dto.ConquistaDTO;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Conquista;
import com.unilivros.repository.ConquistaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConquistaService {
    
    @Autowired
    private ConquistaRepository conquistaRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    public ConquistaDTO criarConquista(ConquistaDTO conquistaDTO) {
        Conquista conquista = modelMapper.map(conquistaDTO, Conquista.class);
        conquista = conquistaRepository.save(conquista);
        
        return modelMapper.map(conquista, ConquistaDTO.class);
    }
    
    @Transactional(readOnly = true)
    public ConquistaDTO buscarPorId(Long id) {
        Conquista conquista = conquistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conquista", id));
        return modelMapper.map(conquista, ConquistaDTO.class);
    }
    
    @Transactional(readOnly = true)
    public List<ConquistaDTO> listarTodas() {
        return conquistaRepository.findAll().stream()
                .map(conquista -> modelMapper.map(conquista, ConquistaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ConquistaDTO> buscarPorTipo(Conquista.TipoConquista tipo) {
        return conquistaRepository.findByTipo(tipo).stream()
                .map(conquista -> modelMapper.map(conquista, ConquistaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ConquistaDTO> buscarDisponiveisPorXp(Integer xp) {
        return conquistaRepository.findByXpNecessarioLessThanEqualOrderByXpNecessarioDesc(xp).stream()
                .map(conquista -> modelMapper.map(conquista, ConquistaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ConquistaDTO> buscarNaoDisponiveisPorXp(Integer xp) {
        return conquistaRepository.findByXpNecessarioGreaterThanOrderByXpNecessarioAsc(xp).stream()
                .map(conquista -> modelMapper.map(conquista, ConquistaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ConquistaDTO> buscarPorNome(String nome) {
        return conquistaRepository.findByNomeContaining(nome).stream()
                .map(conquista -> modelMapper.map(conquista, ConquistaDTO.class))
                .collect(Collectors.toList());
    }
    
    public ConquistaDTO atualizarConquista(Long id, ConquistaDTO conquistaDTO) {
        Conquista conquistaExistente = conquistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conquista", id));
        
        modelMapper.map(conquistaDTO, conquistaExistente);
        conquistaExistente = conquistaRepository.save(conquistaExistente);
        
        return modelMapper.map(conquistaExistente, ConquistaDTO.class);
    }
    
    public void deletarConquista(Long id) {
        if (!conquistaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Conquista", id);
        }
        conquistaRepository.deleteById(id);
    }
}
