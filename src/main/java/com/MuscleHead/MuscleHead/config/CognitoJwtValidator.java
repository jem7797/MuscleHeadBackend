package com.MuscleHead.MuscleHead.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

@Component
public class CognitoJwtValidator {

    private static final Logger logger = LoggerFactory.getLogger(CognitoJwtValidator.class);

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final String userPoolId;
    private final String region;
    private final String appClientId;

    public CognitoJwtValidator(
            @Value("${aws.cognito.userPoolId}") String userPoolId,
            @Value("${aws.cognito.region}") String region,
            @Value("${aws.cognito.appClientId:}") String appClientId) {
        this.userPoolId = userPoolId;
        this.region = region;
        this.appClientId = appClientId;

        try {
            // Construct JWKS URL for Cognito
            String jwksUrl = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json", region,
                    userPoolId);
            logger.info("Initializing Cognito JWT validator with JWKS URL: {}", jwksUrl);

            // Create JWK source from remote URL (Cognito's JWKS endpoint)
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUrl));

            // Create JWT processor
            jwtProcessor = new DefaultJWTProcessor<>();
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                    JWSAlgorithm.RS256, jwkSource);
            jwtProcessor.setJWSKeySelector(keySelector);

            logger.info("Cognito JWT validator initialized successfully");
        } catch (MalformedURLException e) {
            logger.error("Failed to initialize Cognito JWT validator with malformed JWKS URL", e);
            throw new RuntimeException("Failed to initialize Cognito JWT validator", e);
        }
    }

    /**
     * Validates a JWT token from AWS Cognito
     * 
     * @param token The JWT token string
     * @return JWTClaimsSet if valid, null otherwise
     */
    public JWTClaimsSet validateToken(String token) {
        try {
            // Parse and verify the token
            JWTClaimsSet claimsSet = jwtProcessor.process(token, null);

            // Validate issuer
            String expectedIssuer = String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId);
            String actualIssuer = claimsSet.getIssuer();
            if (!expectedIssuer.equals(actualIssuer)) {
                logger.warn("Token issuer mismatch. Expected: {}, Actual: {}", expectedIssuer, actualIssuer);
                return null;
            }

            // Validate audience if app client ID is configured
            if (appClientId != null && !appClientId.isEmpty()) {
                Object audience = claimsSet.getClaim("aud");
                boolean audienceValid = false;
                
                if (audience != null) {
                    // Handle both String and List<String> audience claims
                    if (audience instanceof String) {
                        audienceValid = appClientId.equals(audience);
                    } else if (audience instanceof java.util.List) {
                        audienceValid = ((java.util.List<?>) audience).contains(appClientId);
                    }
                }
                
                if (!audienceValid) {
                    logger.warn("Token audience mismatch. Expected: {}, Actual: {}", appClientId, audience);
                    return null;
                }
            }

            // Validate token use (should be "id" for ID tokens)
            String tokenUse = claimsSet.getClaim("token_use") != null
                    ? claimsSet.getClaim("token_use").toString()
                    : null;
            if (tokenUse != null && !"id".equals(tokenUse) && !"access".equals(tokenUse)) {
                logger.warn("Invalid token_use: {}", tokenUse);
                return null;
            }

            // Validate expiration (already checked by processor, but double-check)
            if (claimsSet.getExpirationTime() != null &&
                    claimsSet.getExpirationTime().before(new java.util.Date())) {
                logger.warn("Token has expired");
                return null;
            }

            logger.debug("Token validated successfully for subject: {}", claimsSet.getSubject());
            return claimsSet;

        } catch (ParseException e) {
            logger.error("Failed to parse JWT token", e);
            return null;
        } catch (BadJOSEException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return null;
        } catch (JOSEException e) {
            logger.error("JOSE exception while validating token", e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error validating token", e);
            return null;
        }
    }

    /**
     * Extracts the Cognito subject (sub) from the token claims
     */
    public String extractSubject(JWTClaimsSet claimsSet) {
        return claimsSet != null ? claimsSet.getSubject() : null;
    }

    /**
     * Extracts the email from the token claims
     */
    public String extractEmail(JWTClaimsSet claimsSet) {
        if (claimsSet == null) {
            return null;
        }
        Object emailClaim = claimsSet.getClaim("email");
        return emailClaim != null ? emailClaim.toString() : null;
    }
}
