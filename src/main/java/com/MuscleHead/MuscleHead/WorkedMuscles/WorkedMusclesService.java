package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.MuscleHead.MuscleHead.Movement.MovementRepository;
import com.MuscleHead.MuscleHead.cache.RedisService;

import jakarta.transaction.Transactional;

@Service
public class WorkedMusclesService {

    private static final Logger logger = LoggerFactory.getLogger(WorkedMusclesService.class);
    private static final String CACHE_PREFIX = "workedMuscles:v2:";
    private static final int DEFAULT_CACHE_TTL_SECONDS = 300; // 5 minutes

    /** Canonical muscle group names stored in DB (fine-grained, no Arms) */
    private static final Set<String> CANONICAL_NAMES = Set.of(
            "Chest", "Triceps", "Biceps", "Forearms", "Shoulders", "Back", "Legs", "Glutes", "Calves", "Abs", "Core",
            "Traps");

    /**
     * Raw areaOfActivation tokens → canonical name (direct mapping, no collapsing)
     */
    private static final Map<String, String> RAW_TO_CANONICAL = Map.ofEntries(
            Map.entry("chest", "Chest"),
            Map.entry("triceps", "Triceps"),
            Map.entry("biceps", "Biceps"),
            Map.entry("forearms", "Forearms"),
            Map.entry("delts", "Shoulders"),
            Map.entry("lats", "Back"),
            Map.entry("quads", "Legs"),
            Map.entry("hamstrings", "Legs"),
            Map.entry("glutes", "Glutes"),
            Map.entry("calves", "Calves"),
            Map.entry("abs", "Abs"),
            Map.entry("obliques", "Core"),
            Map.entry("traps", "Traps"));

    /** Canonical (stored) → muscle IDs for front view */
    private static final Map<String, List<String>> CANONICAL_TO_FRONT = Map.ofEntries(
            Map.entry("Chest", List.of("pecs")),
            Map.entry("Triceps", List.of("triceps")),
            Map.entry("Biceps", List.of("biceps")),
            Map.entry("Forearms", List.of("forearms")),
            Map.entry("Shoulders", List.of("delts")),
            Map.entry("Legs", List.of("quads")),
            Map.entry("Calves", List.of("calves")),
            Map.entry("Abs", List.of("abs")),
            Map.entry("Core", List.of("obliques")),
            Map.entry("Back", List.of()),
            Map.entry("Glutes", List.of()),
            Map.entry("Traps", List.of()));

    /** Canonical (stored) → muscle IDs for back view */
    private static final Map<String, List<String>> CANONICAL_TO_BACK = Map.ofEntries(
            Map.entry("Triceps", List.of("triceps")),
            Map.entry("Forearms", List.of("forearms")),
            Map.entry("Shoulders", List.of("delts")),
            Map.entry("Back", List.of("lats")),
            Map.entry("Legs", List.of("hamstrings")),
            Map.entry("Glutes", List.of("glutes")),
            Map.entry("Calves", List.of("calves")),
            Map.entry("Core", List.of("obliques")),
            Map.entry("Traps", List.of("traps")),
            Map.entry("Chest", List.of()),
            Map.entry("Biceps", List.of()),
            Map.entry("Abs", List.of()));

    @Autowired
    private WorkedMusclesRepository repository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${worked-muscles.cache.ttl-seconds:" + DEFAULT_CACHE_TTL_SECONDS + "}")
    private int cacheTtlSeconds;

    /**
     * Upsert worked muscle groups from a session's exercises.
     * Caller must ensure the authenticated user matches userId (or has permission).
     */
    @Transactional
    public void upsertFromExercises(String userId, List<WorkedMusclesPostRequest.ExerciseInput> exercises) {
        if (userId == null || userId.isBlank() || exercises == null || exercises.isEmpty()) {
            return;
        }

        Set<String> canonicalFromSession = new HashSet<>();
        for (WorkedMusclesPostRequest.ExerciseInput ex : exercises) {
            if (ex.getExerciseId() == null)
                continue;
            movementRepository.findById(ex.getExerciseId()).ifPresent(movement -> {
                logger.info("[WorkedMuscles] user={} exerciseId={} exercise=\"{}\" areaOfActivation=\"{}\"",
                        userId, ex.getExerciseId(), movement.getName(), movement.getAreaOfActivation());
                Set<String> canonical = rawAreasToCanonical(movement.getAreaOfActivation());
                canonicalFromSession.addAll(canonical);
            });
        }

        if (canonicalFromSession.isEmpty()) {
            logger.debug("No canonical muscle groups from exercises for user {}", userId);
            return;
        }

        Optional<WorkedMuscles> existing = repository.findByUserId(userId);
        Set<String> merged = new HashSet<>(canonicalFromSession);
        if (existing.isPresent()) {
            List<String> stored = existing.get().getMuscleGroups();
            if (stored != null) {
                merged.addAll(stored);
            }
        }

        List<String> toStore = merged.stream()
                .filter(CANONICAL_NAMES::contains)
                .sorted()
                .collect(Collectors.toList());

        WorkedMuscles entity = existing.orElse(new WorkedMuscles());
        entity.setUserId(userId);
        entity.setMuscleGroups(toStore);
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);

        bustCache(userId);
        logger.debug("Upserted worked muscles for user {}: {}", userId, toStore);
    }

    /**
     * Get worked muscles for a user, translated to SVG IDs for frontend.
     * Returns cached response when available.
     */
    public WorkedMusclesResponse getWorkedMuscles(String userId) {
        if (userId == null || userId.isBlank()) {
            return emptyResponse();
        }

        String cacheKey = CACHE_PREFIX + userId;
        try {
            String cached = redisService.get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                return parseCachedResponse(cached);
            }
        } catch (Exception e) {
            logger.warn("Failed to read worked muscles cache for {}: {}", userId, e.getMessage());
        }

        WorkedMusclesResponse response = buildResponseFromDb(userId);
        try {
            String json = objectMapper.writeValueAsString(response);
            redisService.setWithTtl(cacheKey, json, cacheTtlSeconds);
        } catch (Exception e) {
            logger.warn("Failed to cache worked muscles for {}: {}", userId, e.getMessage());
        }
        return response;
    }

    private WorkedMusclesResponse buildResponseFromDb(String userId) {
        Optional<WorkedMuscles> opt = repository.findByUserId(userId);
        if (opt.isEmpty() || opt.get().getMuscleGroups() == null || opt.get().getMuscleGroups().isEmpty()) {
            return emptyResponse();
        }

        List<String> canonical = opt.get().getMuscleGroups();
        Set<String> frontSet = new LinkedHashSet<>();
        Set<String> backSet = new LinkedHashSet<>();

        for (String c : canonical) {
            CANONICAL_TO_FRONT.getOrDefault(c, List.of()).forEach(frontSet::add);
            CANONICAL_TO_BACK.getOrDefault(c, List.of()).forEach(backSet::add);
        }

        return new WorkedMusclesResponse(new ArrayList<>(frontSet), new ArrayList<>(backSet));
    }

    private static WorkedMusclesResponse emptyResponse() {
        return new WorkedMusclesResponse(List.of(), List.of());
    }

    private Set<String> rawAreasToCanonical(String areaOfActivation) {
        Set<String> result = new HashSet<>();
        logger.debug("areaOfActivation raw value: {}", areaOfActivation);
        if (areaOfActivation == null || areaOfActivation.isBlank())
            return result;

        // Sanitize PostgreSQL array literal format: "{Chest,Triceps,Delts}" -> "Chest,Triceps,Delts"
        String sanitized = areaOfActivation.trim();
        if (sanitized.startsWith("{")) {
            sanitized = sanitized.substring(1);
        }
        if (sanitized.endsWith("}")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }

        String[] tokens = sanitized.split(",");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        logger.debug("parsed tokens: {}", java.util.Arrays.asList(tokens));
        for (String token : tokens) {
            if (token.isBlank()) continue;
            String normalized = token.toLowerCase();
            if (RAW_TO_CANONICAL.containsKey(normalized)) {
                result.add(RAW_TO_CANONICAL.get(normalized));
            }
        }
        logger.debug("canonical groups: {}", result);
        return result;
    }

    private void bustCache(String userId) {
        try {
            redisService.delete(CACHE_PREFIX + userId);
            logger.debug("Busted worked muscles cache for user {}", userId);
        } catch (Exception e) {
            logger.warn("Failed to bust worked muscles cache for {}: {}", userId, e.getMessage());
        }
    }

    private WorkedMusclesResponse parseCachedResponse(String json) {
        try {
            return objectMapper.readValue(json, WorkedMusclesResponse.class);
        } catch (Exception e) {
            logger.warn("Failed to parse cached worked muscles: {}", e.getMessage());
            return emptyResponse();
        }
    }
}
