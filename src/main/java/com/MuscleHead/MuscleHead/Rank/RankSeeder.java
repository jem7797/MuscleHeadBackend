package com.MuscleHead.MuscleHead.Rank;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RankSeeder implements CommandLineRunner {

    private final RankRepository rankRepository;

    public RankSeeder(RankRepository rankRepository) {
        this.rankRepository = rankRepository;
    }

    @Override
    public void run(String... args) {
        seedRanks();
    }

    public void seedRanks() {
        if (rankRepository.count() > 0) {
            return;
        }

        List<Rank> ranks = List.of(
                createRank(0, "Newbie"),
                createRank(1, "Motivated"),
                createRank(2, "Active"),
                createRank(3, "Consistent"),
                createRank(4, "Dedicated"),
                createRank(5, "Trained"),
                createRank(6, "Athletic"),
                createRank(7, "Plate Pusher"),
                createRank(8, "Metal Head"),
                createRank(9, "Steel Stacker"),
                createRank(10, "Chalked"),
                createRank(11, "Powerhouse"),
                createRank(12, "Advanced"),
                createRank(13, "Elite"),
                createRank(14, "Juggernaut"),
                createRank(15, "Titan"),
                createRank(16, "Ironborn"),
                createRank(17, "Gym Rat"),
                createRank(18, "Olympian"),
                createRank(19, "Herculean")
        );

        rankRepository.saveAll(ranks);
    }

    private Rank createRank(Integer level, String name) {
        Rank rank = new Rank();
        rank.setLevel(level);
        rank.setName(name);
        return rank;
    }
}
