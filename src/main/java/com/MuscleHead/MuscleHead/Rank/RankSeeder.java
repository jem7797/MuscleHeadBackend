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
                createRank(1, "Newbie"),
                createRank(2, "Motivated"),
                createRank(3, "Active"),
                createRank(4, "Consistent"),
                createRank(5, "Dedicated"),
                createRank(6, "Trained"),
                createRank(7, "Athletic"),
                createRank(8, "Plate Pusher"),
                createRank(9, "Metal Head"),
                createRank(10, "Steel Stacker"),
                createRank(11, "Chalked"),
                createRank(12, "Powerhouse"),
                createRank(13, "Advanced"),
                createRank(14, "Elite"),
                createRank(15, "Juggernaut"),
                createRank(16, "Titan"),
                createRank(17, "Ironborn"),
                createRank(18, "Gym Rat"),
                createRank(19, "Olympian"),
                createRank(20, "Herculean")
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
