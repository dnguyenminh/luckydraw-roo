package vn.com.fecredit.app.controller.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.service.EventLocationService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/event-locations")
public class EventLocationController {

    private final EventLocationService eventLocationService;

    @Autowired
    public EventLocationController(EventLocationService eventLocationService) {
        this.eventLocationService = eventLocationService;
    }

    @GetMapping
    public ResponseEntity<List<EventLocation>> getAllEventLocations() {
        List<EventLocation> locations = eventLocationService.findAll();
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventLocation> getEventLocationById(@PathVariable("id") Long id) {
        Optional<EventLocation> location = eventLocationService.findById(id);
        return location.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<EventLocation> createEventLocation(@RequestBody EventLocation location) {
        EventLocation createdLocation = eventLocationService.save(location);
        return new ResponseEntity<>(createdLocation, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventLocation> updateEventLocation(@PathVariable("id") Long id, @RequestBody EventLocation location) {
        if (!eventLocationService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        location.setId(id);
        EventLocation updatedLocation = eventLocationService.save(location);
        return new ResponseEntity<>(updatedLocation, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteEventLocation(@PathVariable("id") Long id) {
        try {
            eventLocationService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventLocation>> getLocationsByEventId(@PathVariable("eventId") Long eventId) {
        List<EventLocation> locations = eventLocationService.findByEventId(eventId);
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }
}
