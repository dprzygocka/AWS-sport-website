package server.sport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import server.sport.exception.ResourceNotFoundException;
import server.sport.model.Sport;
import server.sport.repository.SportRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SportService {
    @Autowired
    SportRepository sportRepository;

    public ResponseEntity<Sport> createSport (Sport sport) {
        Optional<Sport> _sport = sportRepository.findBySportName(sport.getSportName());
        if(_sport.isPresent()) {
            Sport newSportEntry = sportRepository.save(sport); //newSportEntry has the new id which was not existing when passing sport
            return new ResponseEntity<>(newSportEntry, HttpStatus.CREATED);
        }else{
            return new ResponseEntity<>(_sport.get(), HttpStatus.IM_USED);
        }
    }

    public ResponseEntity <Sport> updateSport (int sportId, Sport sport){

        Sport updatedSportEntry = sportRepository.findById(sportId).
                orElseThrow(() -> new ResourceNotFoundException("Not found with id = " + sportId));
        updatedSportEntry.setSportName(sport.getSportName());

        return new ResponseEntity<>(sportRepository.save(updatedSportEntry), HttpStatus.OK) ;
    }
    public ResponseEntity<List<Sport>> getAllSports(){

        List<Sport> sportList = sportRepository.findAll();

        if (sportList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else {
            return new ResponseEntity<>(sportList, HttpStatus.OK);
        }
    }
}
