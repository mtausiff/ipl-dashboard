package com.mtausiff.ipldashboard.controller;

import com.mtausiff.ipldashboard.batchprocessing.JobCompletionNotificationListener;
import com.mtausiff.ipldashboard.model.Team;
import com.mtausiff.ipldashboard.repository.MatchRepository;
import com.mtausiff.ipldashboard.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeamController {
    private final Logger log = LoggerFactory.getLogger(TeamController.class);
    private TeamRepository teamRepository;
    private MatchRepository matchRepository;

    public TeamController(TeamRepository teamRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository= matchRepository;
    }

    @GetMapping("team/{teamName}")
    public Team getTeamInfo(@PathVariable String teamName){
        log.info("********* Get Team information sorted by Date *********");
        Team team = this.teamRepository.findByTeamName(teamName);
        team.setMatches(this.matchRepository.getLatestMatchesByTeam(teamName, 3));
        log.info("********* return team information *********"+team.toString());
        return team;
    }
}
