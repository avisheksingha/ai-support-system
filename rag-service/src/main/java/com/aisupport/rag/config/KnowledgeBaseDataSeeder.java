package com.aisupport.rag.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.aisupport.rag.entity.EmbeddingStatus;
import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.entity.KnowledgeArticleStatus;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1) // Run before KnowledgeDataInitializer (which should be @Order(2))
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseDataSeeder implements CommandLineRunner {

    private final KnowledgeArticleRepository repository;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            log.info("Knowledge Base contains data. Checking for uncategorized articles...");
            List<KnowledgeArticle> uncategorized = repository.findAll().stream()
                    .filter(a -> a.getCategory() == null || a.getCategory().isBlank() || "-".equals(a.getCategory()))
                    .peek(a -> a.setCategory(inferCategory(a.getTitle(), a.getContent())))
                    .toList();
            if (!uncategorized.isEmpty()) {
                repository.saveAll(uncategorized);
                log.info("Categorized {} existing knowledge articles.", uncategorized.size());
            }
            return;
        }

        log.info("Knowledge Base is empty. Seeding initial data...");
        Resource resource = resourceLoader.getResource("classpath:data.sql.bak");
        if (!resource.exists()) {
            log.warn("Seed data file not found.");
            return;
        }

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String sqlContent = FileCopyUtils.copyToString(reader);
            
            // Regex to match ('Title', 'Content', 'PENDING'
            Pattern pattern = Pattern.compile("\\('([^']+)',\\s*'([^']+)',\\s*'PENDING'");
            Matcher matcher = pattern.matcher(sqlContent);

            List<KnowledgeArticle> articles = new ArrayList<>();
            while (matcher.find()) {
                String title = matcher.group(1).replace("''", "'");
                String content = matcher.group(2).replace("''", "'");
                
                KnowledgeArticle article = KnowledgeArticle.builder()
                        .title(title)
                        .content(content)
                        .category(inferCategory(title, content))
                        .embeddingStatus(EmbeddingStatus.PENDING)
                        .status(KnowledgeArticleStatus.PUBLISHED)
                        .version(1L)
                        .build();
                articles.add(article);
            }

            if (!articles.isEmpty()) {
                repository.saveAll(articles);
                log.info("Successfully seeded {} knowledge articles with business categories.", articles.size());
            } else {
                log.warn("No articles parsed from seed data.");
            }
        }
    }

    private static String inferCategory(String title, String content) {
        String text = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
        if (text.contains("login") || text.contains("password") || text.contains("auth") || text.contains("mfa") || text.contains("2fa") || text.contains("sso") || text.contains("token") || text.contains("jwt") || text.contains("username") || text.contains("lockout")) {
            return "Authentication";
        }
        if (text.contains("bill") || text.contains("invoice") || text.contains("subscription") || text.contains("plan") || text.contains("charge") || text.contains("pricing") || text.contains("receipt")) {
            return "Billing";
        }
        if (text.contains("payment") || text.contains("card") || text.contains("refund") || text.contains("checkout") || text.contains("stripe") || text.contains("transaction") || text.contains("failure")) {
            return "Payments";
        }
        if (text.contains("user") || text.contains("account") || text.contains("profile") || text.contains("role") || text.contains("permission") || text.contains("team") || text.contains("member") || text.contains("management")) {
            return "User Management";
        }
        if (text.contains("security") || text.contains("lock") || text.contains("audit") || text.contains("encrypt") || text.contains("privacy") || text.contains("vulnerability") || text.contains("shield")) {
            return "Security";
        }
        if (text.contains("api") || text.contains("webhook") || text.contains("endpoint") || text.contains("rest") || text.contains("graphql") || text.contains("sdk") || text.contains("integration")) {
            return "API";
        }
        if (text.contains("network") || text.contains("ip") || text.contains("dns") || text.contains("vpn") || text.contains("gateway") || text.contains("connection") || text.contains("firewall") || text.contains("timeout")) {
            return "Networking";
        }
        if (text.contains("compliance") || text.contains("gdpr") || text.contains("hipaa") || text.contains("soc2") || text.contains("policy") || text.contains("term") || text.contains("sla") || text.contains("legal")) {
            return "Compliance";
        }
        return "General";
    }
}
