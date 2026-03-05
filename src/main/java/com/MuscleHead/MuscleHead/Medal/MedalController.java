package com.MuscleHead.MuscleHead.Medal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.config.SecurityUtils;

@RestController
@RequestMapping("medal/api/")
public class MedalController {

    private static final Logger logger = LoggerFactory.getLogger(MedalController.class);

    @Autowired
    private MedalService medalService;

    @GetMapping
    public ResponseEntity<List<MedalResponse>> getMedals() {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<MedalResponse> medals = medalService.getMedalsForUser(subId);
        logger.debug("Returning {} medals for user {}", medals.size(), subId);
        return ResponseEntity.ok(medals);
    }
}
