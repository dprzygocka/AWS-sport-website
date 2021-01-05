package server.sport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.sport.model.Sport;
import server.sport.service.SportService;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping ("/api/sports")
public class SportController {

    @Autowired
    SportService sportService;

    //At this stage it is simply making sport name, and is not concerned with the teams that correspond with the sport
    @PostMapping ()
    public ResponseEntity <Sport> createSport (@RequestBody Sport sport) {
        return sportService.createSport(sport);
    }

    @PutMapping (path="/{sport_id}", consumes="application/json")
    public ResponseEntity <Sport> updateSport (@PathVariable ("sport_id") int sportId, @RequestBody Sport sport){
        return sportService.updateSport(sportId, sport);
    }

    @GetMapping
    public ResponseEntity<List<Sport>> getAllSports(){
        return sportService.getAllSports();
    }

}
