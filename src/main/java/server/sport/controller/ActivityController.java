package server.sport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import server.sport.model.Activity;
import server.sport.model.Responsibility;
import server.sport.service.ActivityService;

import java.util.Collection;
import java.util.Map;


@CrossOrigin
@RequestMapping("/api/activities")
@RestController
public class ActivityController {

    @Autowired
    ActivityService activityService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPageOfActivities(
            @RequestParam(defaultValue = "0")int page, //activities to be loaded on page 0
            @RequestParam(defaultValue = "3")int size, //3 activities will be fetched from the database
            @RequestParam(defaultValue = "activityId,desc")String[] sort //ordered by descending order
    ){
        return activityService.getPageOfActivities(page, size, sort);
    }


    @GetMapping("/{activity_id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable("activity_id") int activityId){
        return activityService.getActivityById(activityId);
    }

    @GetMapping("/teamActivities/{team_id}")
    public ResponseEntity<Collection<Activity>> getActivitiesForTeam(@PathVariable("team_id") int teamId){
        return activityService.getActivitiesForTeam(teamId);
    }

    //Put responsibility into activity
    @PutMapping("/{activity_id}")
    public ResponseEntity<Activity> updateActivityInformation (@PathVariable("activity_id") int activityId, @RequestBody(required = false) Responsibility responsibility){
        return activityService.updateActivityInformation(activityId, responsibility);
    }

    @Transactional(rollbackFor = ResourceNotFoundException.class)
    @PostMapping
    public ResponseEntity<Activity> createActivity (@RequestBody Activity activity){
        return activityService.createActivity(activity);
    }
}
