package com.unilivros.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import com.unilivros.dto.LivroDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IAService {
    private OrtEnvironment env;
    private OrtSession session;

    // Construtor: Carrega o modelo ONNX ao iniciar a aplicação
//    public IAService() {
//        try {
//            this.env = OrtEnvironment.getEnvironment();
//// IMPORTANTE: O arquivo .onnx deve estar na raiz do projeto (mesma pasta do pom.xml)
//            String modelPath = getClass().getClassLoader()
//                    .getResource("ia/modelo_nivel_leitura.onnx")
//                    .getPath();
//            this.session = env.createSession(modelPath);
//            System.out.println("✅ IA INICIADA: Modelo carregado com sucesso!");
//        } catch (Exception e) {
//            System.err.println("⚠ AVISO IA: Não foi possível carregar o modelo. " + e.getMessage());
    //// Não paramos a aplicação, mas a IA ficará indisponível
//        }
//    }
    public IAService() {
        try {
            this.env = OrtEnvironment.getEnvironment();

            // Carrega o modelo do classpath
            var resource = getClass()
                    .getClassLoader()
                    .getResourceAsStream("modelo_nivel_leitura.onnx");

            if (resource == null) {
                System.err.println("❌ ERRO: Arquivo ONNX não encontrado no classpath!");
                return;
            }

            // Copia o arquivo para um arquivo temporário, porque o ONNX Runtime precisa de um caminho físico
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("modelo_nivel_leitura", ".onnx");
            java.nio.file.Files.copy(resource, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            this.session = env.createSession(tempFile.toString());

            System.out.println("✅ IA INICIADA: Modelo carregado com sucesso!");

        } catch (Exception e) {
            System.err.println("⚠ AVISO IA: Não foi possível carregar o modelo. " + e.getMessage());
        }
    }


    public String preverNivel(LivroDTO livro) {
        if (session == null) return "IA Indisponível";
        try {
            // === 1. PREPARAÇÃO DOS DADOS (Inputs) ===
            // Convertendo dados do DTO para o formato que a IA espera
            // Tratamento de nulos para evitar erros
            float anoVal = livro.getAno() != null ? (float) livro.getAno() : 2024f;
            String generoVal = livro.getGenero() != null ? livro.getGenero() : "Desconhecido";
            String editoraVal = livro.getEditora() != null ? livro.getEditora() : "Desconhecida";
            // Calculando o tamanho da descrição (feature 'tam_descricao')
            float tamDescVal = 0f;
            if (livro.getDescricao() != null) {
                tamDescVal = (float) livro.getDescricao().length();
            }
            // === 2. CRIAÇÃO DOS TENSORES (Arrays ONNX) ===
            // Arrays de tamanho 1, pois analisamos 1 livro por vez
            // Numéricos (Float)
            float[] anoArr = new float[]{anoVal};
            float[] tamDescArr = new float[]{tamDescVal};
            // Texto (String)
            String[] generoArr = new String[]{generoVal};
            String[] editoraArr = new String[]{editoraVal};
            OnnxTensor tAno = OnnxTensor.createTensor(env, anoArr);
            OnnxTensor tTamDesc = OnnxTensor.createTensor(env, tamDescArr);
            OnnxTensor tGenero = OnnxTensor.createTensor(env, generoArr);
            OnnxTensor tEditora = OnnxTensor.createTensor(env, editoraArr);
            // === 3. MAPEAMENTO (Nomes EXATOS do seu modelo) ===
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("ano", tAno);
            inputs.put("tam_descricao", tTamDesc);
            inputs.put("genero", tGenero);
            inputs.put("editora", tEditora);
            // === 4. EXECUÇÃO DA PREVISÃO ===
            try (Result results = session.run(inputs)) {
                // Pega o primeiro output da IA
                Object output = results.get(0).getValue();
                // Trata o retorno (pode ser Array de String ou números)
                if (output instanceof String[]) {
                    return ((String[]) output)[0];
                } else if (output instanceof long[]) {
                    return String.valueOf(((long[]) output)[0]);
                }
                return output.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro na Análise";
        }
    }
}