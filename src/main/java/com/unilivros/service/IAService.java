package com.unilivros.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import com.unilivros.dto.LivroDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class IAService {

    private OrtEnvironment env;
    private OrtSession session;

    public IAService() {
        try {
            this.env = OrtEnvironment.getEnvironment();

            ClassPathResource resource = new ClassPathResource("modelo_nivel_leitura.onnx");

            if (!resource.exists()) {
                throw new RuntimeException("Arquivo 'modelo_nivel_leitura.onnx' não encontrado em src/main/resources");
            }

            File modelTempFile = File.createTempFile("modelo_nivel_leitura", ".onnx");
            modelTempFile.deleteOnExit(); // Garante que o arquivo suma quando o app fechar

            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, modelTempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            this.session = env.createSession(modelTempFile.getAbsolutePath());

            System.out.println("✅ IA INICIADA: Modelo carregado com sucesso!");

        } catch (Exception e) {
            System.err.println("⚠️ AVISO IA: Não foi possível carregar o modelo. " + e.getMessage());
        }
    }

    public String preverNivel(LivroDTO livro) {
        if (session == null) return "IA Indisponível";

        try {
            // 1. Preparação dos dados brutos
            float anoVal = livro.getAno() != null ? (float) livro.getAno() : 2024f;
            String generoVal = livro.getGenero() != null ? livro.getGenero() : "Desconhecido";
            String editoraVal = livro.getEditora() != null ? livro.getEditora() : "Desconhecida";

            float tamDescVal = 0f;
            if (livro.getDescricao() != null) {
                tamDescVal = (float) livro.getDescricao().length();
            }

            // 2. Criação dos Arrays 2D (Rank 2) OBRIGATÓRIOS
            // O formato deve ser [1][1] (1 linha, 1 coluna)

            // Para 'ano'
            float[][] anoArr = new float[1][1];
            anoArr[0][0] = anoVal;

            // Para 'tam_descricao'
            float[][] tamDescArr = new float[1][1];
            tamDescArr[0][0] = tamDescVal;

            // Para 'genero'
            String[][] generoArr = new String[1][1];
            generoArr[0][0] = generoVal;

            // Para 'editora' (onde estava dando o erro agora)
            String[][] editoraArr = new String[1][1];
            editoraArr[0][0] = editoraVal;

            // 3. Criação dos Tensores a partir dos Arrays 2D
            OnnxTensor tAno = OnnxTensor.createTensor(env, anoArr);
            OnnxTensor tTamDesc = OnnxTensor.createTensor(env, tamDescArr);
            OnnxTensor tGenero = OnnxTensor.createTensor(env, generoArr);
            OnnxTensor tEditora = OnnxTensor.createTensor(env, editoraArr);

            // 4. Mapeamento dos inputs
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("ano", tAno);
            inputs.put("tam_descricao", tTamDesc);
            inputs.put("genero", tGenero);
            inputs.put("editora", tEditora);

            // 5. Execução
            try (Result results = session.run(inputs)) {
                Object output = results.get(0).getValue();

                if (output instanceof String[]) {
                    return ((String[]) output)[0]; // Retorna string direta
                } else if (output instanceof long[]) {
                    return String.valueOf(((long[]) output)[0]); // Converte long para string
                } else if (output instanceof float[]) {
                    return String.valueOf(((float[]) output)[0]);
                } else if (output instanceof String[][]) {
                    // Caso o output também seja 2D
                    return ((String[][]) output)[0][0];
                } else if (output instanceof long[][]) {
                    return String.valueOf(((long[][]) output)[0][0]);
                }

                return output.toString();
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar previsão da IA: " + e.getMessage());
            e.printStackTrace();
            return "Erro na Análise";
        }
    }
}