package com.example.customersupportai.service;

import com.example.customersupportai.model.SopDocument;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class SopRepository {

    private final ResourcePatternResolver resourcePatternResolver;
    private final String locationPattern;
    private List<SopDocument> documents = List.of();

    public SopRepository(
            ResourcePatternResolver resourcePatternResolver,
            @Value("${customer-support.sop-location-pattern}") String locationPattern
    ) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.locationPattern = locationPattern;
    }

    @PostConstruct
    void loadDocuments() throws IOException {
        Resource[] resources = resourcePatternResolver.getResources(locationPattern);
        List<SopDocument> loaded = new ArrayList<>();

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) {
                continue;
            }
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            String id = filename.replace(".txt", "").toUpperCase().replace("-", "_");
            loaded.add(new SopDocument(id, filename, content, tokenize(content).size()));
        }

        loaded.sort(Comparator.comparing(SopDocument::id));
        this.documents = List.copyOf(loaded);
    }

    public List<SopDocument> findAll() {
        return documents;
    }

    static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String[] tokens = text.toLowerCase()
                .replaceAll("[^a-z0-9_]+", " ")
                .trim()
                .split("\\s+");

        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isBlank()) {
                result.add(token);
            }
        }
        return result;
    }
}
