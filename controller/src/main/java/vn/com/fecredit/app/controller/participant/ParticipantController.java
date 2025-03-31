package vn.com.fecredit.app.controller.participant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.service.ParticipantService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    @Autowired
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping
    public ResponseEntity<List<Participant>> getAllParticipants() {
        List<Participant> participants = participantService.findAll();
        return new ResponseEntity<>(participants, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Participant> getParticipantById(@PathVariable("id") Long id) {
        Optional<Participant> participant = participantService.findById(id);
        return participant.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Participant> createParticipant(@RequestBody Participant participant) {
        Participant createdParticipant = participantService.save(participant);
        return new ResponseEntity<>(createdParticipant, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Participant> updateParticipant(@PathVariable("id") Long id, @RequestBody Participant participant) {
        if (!participantService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        participant.setId(id);
        Participant updatedParticipant = participantService.save(participant);
        return new ResponseEntity<>(updatedParticipant, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteParticipant(@PathVariable("id") Long id) {
        try {
            participantService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Participant>> getParticipantsByEventId(@PathVariable("eventId") Long eventId) {
        List<Participant> participants = participantService.findByEventId(eventId);
        return new ResponseEntity<>(participants, HttpStatus.OK);
    }

    @GetMapping("/check-in/{eventId}")
    public ResponseEntity<List<Participant>> getCheckedInParticipants(@PathVariable("eventId") Long eventId) {
        List<Participant> participants = participantService.findCheckedInParticipantsByEventId(eventId);
        return new ResponseEntity<>(participants, HttpStatus.OK);
    }
    
    @PostMapping("/{id}/check-in")
    public ResponseEntity<Participant> checkInParticipant(@PathVariable("id") Long id) {
        Optional<Participant> participantOpt = participantService.findById(id);
        if (!participantOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Participant participant = participantOpt.get();
        participant.setCheckedIn(true);
        Participant updatedParticipant = participantService.save(participant);
        return new ResponseEntity<>(updatedParticipant, HttpStatus.OK);
    }
}
