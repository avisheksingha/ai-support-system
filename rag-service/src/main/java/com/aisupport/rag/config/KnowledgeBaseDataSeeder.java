package com.aisupport.rag.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // ── Keyword constants (shared between inferCategory and TAG_SIGNALS) ──────
    private static final String KW_OAUTH        = "oauth";
    private static final String KW_PASSWORD     = "password";
    private static final String KW_TRUSTED_DEV  = "trusted device";
    private static final String KW_PHISHING     = "phishing";
    private static final String KW_IDEMPOTENCY  = "idempotency";
    private static final String KW_PAGINATION   = "pagination";
    private static final String KW_API_QUOTA    = "api quota";
    private static final String KW_PRORATION    = "proration";
    private static final String KW_SUBSCRIPTION = "subscription";
    private static final String KW_HIPAA        = "hipaa";

    // ── Data-driven tag signal map ────────────────────────────────────────────
    // Key = tag name to emit; Value = keywords that trigger it.
    // Insertion order is preserved so tags are stable across runs.
    private static final Map<String, String[]> TAG_SIGNALS;
    static {
        TAG_SIGNALS = new LinkedHashMap<>();
        TAG_SIGNALS.put(KW_OAUTH,           new String[]{ KW_OAUTH, "bearer token", "access token", "refresh token", "token expir" });
        TAG_SIGNALS.put("mfa",              new String[]{ "mfa", "2fa", "multi-factor", "one-time", "backup code" });
        TAG_SIGNALS.put("sso",              new String[]{ "sso", "single sign-on", "saml", "identity provider" });
        TAG_SIGNALS.put(KW_PASSWORD,        new String[]{ KW_PASSWORD, "credential", "lockout", "password reset",
                                                          "password expir", "password reuse", "password policy" });
        TAG_SIGNALS.put("session-management", new String[]{ "session", "session timeout", "remember me",
                                                            "active session", KW_TRUSTED_DEV, "session management" });
        TAG_SIGNALS.put("account-security", new String[]{ KW_PHISHING, "suspicious login", "unrecognized device",
                                                          "compromised account", "unauthorized access" });
        TAG_SIGNALS.put("webhooks",         new String[]{ "webhook", "webhook signature", "webhook retry",
                                                          "webhook delivery", "event type" });
        TAG_SIGNALS.put("rate-limiting",    new String[]{ "rate limit", "rate-limit", "http 429",
                                                          "exponential backoff", "retry", KW_API_QUOTA });
        TAG_SIGNALS.put(KW_IDEMPOTENCY,     new String[]{ KW_IDEMPOTENCY, "idempotent", "duplicate charge" });
        TAG_SIGNALS.put("sdk",              new String[]{ "sdk", "client library", "java sdk", "python sdk", "node" });
        TAG_SIGNALS.put(KW_PAGINATION,      new String[]{ KW_PAGINATION, "page size", "page index" });
        TAG_SIGNALS.put("api-versioning",   new String[]{ "api version", "versioning", "deprecat", "sunset" });
        TAG_SIGNALS.put("http-status",      new String[]{ "http status", "status code", "http 400",
                                                          "http 401", "http 403", "http 500" });
        TAG_SIGNALS.put("developer-portal", new String[]{ "developer portal", "sandbox", "test environment" });
        TAG_SIGNALS.put("refunds",          new String[]{ "refund", "billing adjustment", "payment reversal",
                                                          "credit", "promotional credit" });
        TAG_SIGNALS.put("invoicing",        new String[]{ "invoice", "invoice number" });
        TAG_SIGNALS.put(KW_SUBSCRIPTION,    new String[]{ KW_SUBSCRIPTION, "renewal", "auto billing",
                                                          "plan change", "upgrade", "downgrade" });
        TAG_SIGNALS.put(KW_PRORATION,       new String[]{ KW_PRORATION, "prorated" });
        TAG_SIGNALS.put("promotions",       new String[]{ "coupon", "promo code", "discount", "promotional" });
        TAG_SIGNALS.put("payment-methods",  new String[]{ "payment method", "credit card", "debit card",
                                                          "ach", "wire transfer" });
        TAG_SIGNALS.put("payment-failure",  new String[]{ "grace period", "payment failure", "card declin",
                                                          "subscription renewal" });
        TAG_SIGNALS.put("user-invitation",  new String[]{ "invite", "invitation", "add user", "new user" });
        TAG_SIGNALS.put("roles-permissions", new String[]{ "role", "rbac", "permission", "access control",
                                                           "granular permission", "custom role" });
        TAG_SIGNALS.put("account-lifecycle", new String[]{ "deactivat", "suspend", "reactivat", "remove user",
                                                           "license", "seat" });
        TAG_SIGNALS.put("bulk-operations",  new String[]{ "bulk import", "csv import", "bulk user" });
        TAG_SIGNALS.put("organization",     new String[]{ "organization settings", "org settings", "transfer ownership" });
        TAG_SIGNALS.put("api-key-management", new String[]{ "api key rotation", "key rotation", "revoke",
                                                            "credential leakage" });
        TAG_SIGNALS.put("ip-restrictions",  new String[]{ "ip restriction", "ip whitelist", "allowlist", "allowlisting" });
        TAG_SIGNALS.put("audit-logs",       new String[]{ "audit log", "audit trail", "activity log" });
        TAG_SIGNALS.put("encryption",       new String[]{ "encrypt", "tls", "ssl", "certificate" });
        TAG_SIGNALS.put("incident-response", new String[]{ "incident", "breach", "security incident", "vulnerability" });
        TAG_SIGNALS.put("gdpr",             new String[]{ "gdpr", "data privacy", "right to erase",
                                                          "data rights", "consent" });
        TAG_SIGNALS.put(KW_HIPAA,           new String[]{ KW_HIPAA });
        TAG_SIGNALS.put("certifications",   new String[]{ "soc2", "iso27001", "iso 27001", "certification" });
        TAG_SIGNALS.put("data-governance",  new String[]{ "data retention", "retention policy", "data residency" });
        TAG_SIGNALS.put("data-export",      new String[]{ "data export", "export", "download" });
        TAG_SIGNALS.put("performance",      new String[]{ "timeout", "latency", "slow performance", "performance" });
        TAG_SIGNALS.put("network",          new String[]{ "firewall", "dns", "vpn", "ip address", "proxy" });
        TAG_SIGNALS.put("browser",          new String[]{ "browser", "browser cache", "cache", "cookie" });
        TAG_SIGNALS.put("support",          new String[]{ "support ticket", "open a ticket", "submit a ticket" });
        TAG_SIGNALS.put("notifications",    new String[]{ "notification", "email delivery", "alert", "digest" });
        TAG_SIGNALS.put("file-uploads",     new String[]{ "file upload", "attachment", "file size", "upload limit" });
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            log.info("Knowledge Base contains data. Checking for uncategorized articles...");
            List<KnowledgeArticle> uncategorized = repository.findAll().stream()
                    .filter(a -> a.getCategory() == null || a.getCategory().isBlank() || "-".equals(a.getCategory()))
                    .map(a -> {
                        a.setCategory(inferCategory(a.getTitle(), a.getContent()));
                        return a;
                    })
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
                        .tags(inferTags(title, content))
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

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private static String inferCategory(String title, String content) {
        String text = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();

        // Authentication — sign-in flows, credentials, sessions, MFA, SSO, OAuth tokens
        if (containsAny(text,
                "login", "log in", "sign in", "sign-in", "logout", "sign out",
                KW_PASSWORD, "credential", "username", "lockout", "account recovery",
                "auth", KW_OAUTH, "bearer token", "access token", "refresh token",
                "mfa", "2fa", "multi-factor", "one-time", "backup code",
                "sso", "single sign-on", "saml", "identity provider",
                "jwt", "token expir", "session timeout", "remember me",
                KW_TRUSTED_DEV, "verification link", "email verif", "login history",
                "suspicious login", "unrecognized device", KW_PHISHING)) {
            return "Authentication";
        }

        // API — developer integrations, keys, webhooks, versioning, SDKs
        if (containsAny(text,
                "api key", "api keys", "api authentication", "api request", KW_API_QUOTA,
                "api version", "api versioning", "api deprecat", "api best practice",
                "webhook", "endpoint", "rest", "graphql", "sdk", "integration",
                "http 400", "http 401", "http 403", "http 404", "http 409", "http 410",
                "http 429", "http 500", "status code", "rate limit", "rate-limit",
                KW_IDEMPOTENCY, "idempotent", "exponential backoff", "retry",
                KW_PAGINATION, "request validation", "payload", "json body",
                "developer portal", "sandbox", "timeout handling", "throughput",
                "deprecation", "client library", "versioning policy")) {
            return "API";
        }

        // Payments — transactions, charges, refunds, cards, invoicing
        if (containsAny(text,
                "payment", "pay ", "paying", "paid", "refund", "checkout",
                "credit card", "debit card", "card declin", "card detail",
                "stripe", "transaction", "ach", "wire transfer",
                "duplicate charge", "payment failure", "payment method",
                "payment reversal", "billing contact", "invoice number", "order id",
                "coupon", "promo code", "promotional credit", KW_PRORATION,
                "auto billing", "manual payment", "outstanding balance",
                "grace period", "billing cycle", "billing delay")) {
            return "Payments";
        }

        // Billing — plans, subscriptions, usage limits, dashboards
        if (containsAny(text,
                "bill", "billing dashboard", "invoice", KW_SUBSCRIPTION,
                "plan", "enterprise plan", "standard plan", "annual plan",
                "charge", "pricing", "receipt", "upgrade", "downgrade",
                "renewal", "storage limit", "workspace limit", "user limit",
                KW_API_QUOTA, "seat", "fair use", "storage quota", "overage",
                "currency", "tax", "discount", "license", "feature availability")) {
            return "Billing";
        }

        // User Management — invitations, roles, deactivation, org settings
        if (containsAny(text,
                "user", "account", "profile", "role", "permission", "team", "member",
                "management", "invite", "invitation", "remove user", "deactivat",
                "suspend", "reactivat", "transfer ownership", "organization settings",
                "bulk import", "bulk user", "csv import", "access control",
                "rbac", "granular permission", "custom role", "assignee",
                "administrator", "agent role", "viewer role")) {
            return "User Management";
        }

        // Security — threats, incidents, encryption, access controls, compliance posture
        if (containsAny(text,
                "security", "lock", "unlock", "audit", "encrypt", "privacy",
                "vulnerability", "shield", "breach", "incident", "compromised",
                KW_PHISHING, "malicious", "ip restriction", "whitelist", "allowlist",
                "brute force", "csrf", "xss", "injection attack",
                "certificate", "tls", "ssl", KW_TRUSTED_DEV, "revoke",
                "api key rotation", "key rotation", "secret", "credential leakage",
                "security alert", "security notification", "security setting",
                "password policy", "password expir", "password reuse",
                "session management", "active session", "sign out all")) {
            return "Security";
        }

        // Networking — infrastructure, connectivity, DNS, proxies
        if (containsAny(text,
                "network", "ip address", "dns", "vpn", "gateway", "firewall",
                "connection", "timeout", "latency", "bandwidth", "proxy",
                "tcp", "udp", "http", "https", "tls handshake", "port",
                "load balancer", "cdn", "throughput", "packet", "traceroute")) {
            return "Networking";
        }

        // Compliance — regulations, audits, certifications, data governance
        if (containsAny(text,
                "compliance", "gdpr", KW_HIPAA, "soc2", "iso27001", "iso 27001",
                "policy", "legal", "regulation", "regulat",
                "data retention", "retention policy", "data residency",
                "data privacy", "consent", "data rights", "right to erase",
                "audit log", "audit trail", "discovery", "certification",
                "data export", "data breach", "privacy setting", "sla", "term")) {
            return "Compliance";
        }

        return "General";
    }

    private static List<String> inferTags(String title, String content) {
        String text = ((title != null ? title : "") + " " + (content != null ? content : "")).toLowerCase();
        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : TAG_SIGNALS.entrySet()) {
            if (containsAny(text, entry.getValue())) {
                tags.add(entry.getKey());
            }
        }
        return tags;
    }
}
