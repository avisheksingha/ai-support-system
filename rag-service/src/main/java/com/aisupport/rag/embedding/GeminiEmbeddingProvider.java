package com.aisupport.rag.embedding;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeminiEmbeddingProvider implements EmbeddingProvider {

    private final VertexAiTextEmbeddingModel embeddingModel;

    public GeminiEmbeddingProvider(VertexAiTextEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public List<Float> embed(String text) {
    	
    	float[] embedding = embeddingModel.embed(text);
    	
    	log.info("Generated embedding of length {} for text: {}", embedding.length, text);

    	return convertToList(embedding);
    }
    
    private List<Float> convertToList(float[] array) {
		List<Float> list = new ArrayList<>(array.length);
		for (float v : array) {
            list.add(v);
        }
		return list;
	}
}