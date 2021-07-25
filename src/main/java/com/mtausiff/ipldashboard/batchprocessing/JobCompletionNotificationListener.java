package com.mtausiff.ipldashboard.batchprocessing;

import com.mtausiff.ipldashboard.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {
    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final EntityManager em;

    @Autowired
    public JobCompletionNotificationListener(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

       //UnComment it to verify data insertion process
            /*jdbcTemplate.query("SELECT team1,team2,date FROM match",
                    (rs, row) -> "Team1 "+rs.getString(1)+" Team2 "+rs.getString(2)+" Date "+rs.getString(3)
            ).forEach(str -> log.info(str));*/
            final String GET_TEAM1_N_TOTALMATCHES_SQL = "SELECT M.team1, COUNT(*) FROM Match M GROUP BY M.team1";
            final String GET_TEAM2_N_TOTALMATCHES_SQL = "select m.team2, count(*) from Match m group by m.team2";
            final String GET_TEAM_N_TOTALWINS_SQL = "select m.winner, count(*) from Match m group by m.winner";

            Map<String, Team> teamData = new HashMap<>();

            log.info("********* Get total matches played by Team2 *********");
        //Get total matches played by Team1
            em.createQuery(GET_TEAM1_N_TOTALMATCHES_SQL, Object[].class)
                .getResultList()
                .stream()
                .map(e -> new Team((String)e[0], (long)e[1]))
                .forEach(team -> teamData.put(team.getTeamName(), team));
            log.info("********* Get total matches played by Team2 *********");
        //Get total matches played by Team2
            em.createQuery(GET_TEAM2_N_TOTALMATCHES_SQL, Object[].class)
                    .getResultList()
                    .stream()
                    .forEach(e -> {
                        Team team = teamData.get((String) e[0]);
                        team.setTotalMatches(team.getTotalMatches()+ (long)e[1]);
                    });
            log.info("********* Get total Wins won by individual Team *********");
            //Get total Wins won by individual Team
            em.createQuery(GET_TEAM_N_TOTALWINS_SQL, Object[].class)
                    .getResultList()
                    .stream()
                    .forEach(e -> {
                        Team team = teamData.get((String) e[0]);
                        if(team!=null) team.setTotalWins((long)e[1]);
                    });
            teamData.values().forEach(team -> em.persist(team));
        // Print Team Object
            log.info("********* Printing Teams information *********");
            teamData.values().forEach(team -> log.info(team.toString()));
        }
    }
}
