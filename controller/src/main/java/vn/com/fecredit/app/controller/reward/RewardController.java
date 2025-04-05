package vn.com.fecredit.app.controller.reward;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.service.RewardService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    @Autowired
    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping
    public ResponseEntity<List<Reward>> getAllRewards() {
        List<Reward> rewards = rewardService.findAll();
        return new ResponseEntity<>(rewards, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reward> getRewardById(@PathVariable("id") Long id) {
        Optional<Reward> reward = rewardService.findById(id);
        return reward.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Reward> createReward(@RequestBody Reward reward) {
        Reward createdReward = rewardService.save(reward);
        return new ResponseEntity<>(createdReward, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reward> updateReward(@PathVariable("id") Long id, @RequestBody Reward reward) {
        if (!rewardService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        reward.setId(id);
        Reward updatedReward = rewardService.save(reward);
        return new ResponseEntity<>(updatedReward, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteReward(@PathVariable("id") Long id) {
        try {
            rewardService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Reward>> getRewardsByEventId(@PathVariable("eventId") Long eventId) {
        List<Reward> rewards = rewardService.findByEventId(eventId);
        return new ResponseEntity<>(rewards, HttpStatus.OK);
    }
}
