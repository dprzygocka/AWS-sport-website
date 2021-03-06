package server.sport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import server.sport.enumerated.ActivityTypeEnum;
import server.sport.enumerated.UserStatusesEnum;
import server.sport.model.*;
import server.sport.repository.*;

import java.util.*;

@Service
public class ActivityService {
    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    ActivityTypeRepository activityTypeRepository;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ResponsibilityRepository responsibilityRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserResponsibilityRepository userResponsibilityRepository;

    @Autowired
    UserStatusRepository userStatusRepository;

    @Autowired
    ActivityStatusRepository activityStatusRepository;

    private Sort.Direction getSortDirection (String direction){

        if (direction.equals("asc")){
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
    }

    public ResponseEntity<Map<String, Object>> getPageOfActivities(int page, int size, String[] sort){

        List<Sort.Order> orders = new ArrayList<>();
        if (sort[0].contains(",")){
            for (String sortOrder : sort){
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
            }

        }else{
            orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
        }

        Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

        //establishes what is on the pageOfActivities.
        Page<Activity> pageOfActivities;
        pageOfActivities = activityRepository.findAll(pagingSort);
        List<Activity> activities = pageOfActivities.getContent();

        if (activities.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("activities", activities);
        response.put("currentPage", pageOfActivities.getNumber());
        response.put("totalActivities", pageOfActivities.getTotalElements());
        response.put("totalPages", pageOfActivities.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Activity> getActivityById(int activityId){

        Activity activity = activityRepository.findById(activityId).orElseThrow(
                () -> new server.sport.exception.ResourceNotFoundException("Not found with id = " + activityId));

        if (activity.equals(null)){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(activity, HttpStatus.OK);
    }

    public ResponseEntity<Collection<Activity>> getActivitiesForTeam(int teamId){
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new server.sport.exception.ResourceNotFoundException("Not found with id = " + teamId));
        Collection <Activity> activities = team.getActivities();

        if (activities.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(activities, HttpStatus.OK);
    }

    public ResponseEntity<Activity> updateActivityInformation (int activityId, Responsibility responsibility){

        Responsibility responsibility1 = responsibilityRepository.findById(responsibility.getResponsibilityId()).orElseThrow(
                () -> new server.sport.exception.ResourceNotFoundException("Not found with id = " + responsibility.getResponsibilityId()));

        Activity activity = activityRepository.findById(activityId).orElseThrow(
                () -> new server.sport.exception.ResourceNotFoundException("Not found with id = " + activityId));

        userResponsibilityRepository.saveResponsibilityForActivity(responsibility1.getResponsibilityId(), activityId);

        Activity _activity = activityRepository.findById(activityId).orElseThrow(
                () -> new server.sport.exception.ResourceNotFoundException("Not found with id = " + activityId));

        return (new ResponseEntity<>(_activity, HttpStatus.CREATED));
    }

    private Location getNewActivityLocation(Location _location){
        Location location;
        try {
            location = locationRepository.findById(_location.getLocationId()).get();
        }catch(ResourceNotFoundException e){
            throw new ResourceNotFoundException("Provide location information under reservation object. " +
                    "Missing id if the location exist or a name of a new location");
        }
        return location;
    }

    private Reservation getNewActivityReservation(Location location, Reservation _reservation){
        Reservation reservation;
        try {
            _reservation.setLocation(location);
            reservation = reservationRepository.save(_reservation);
        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Cannot save the reservation. Check whether date and time format is correct");
        }
        return reservation;
    }

    private ActivityType getNewActivityActivityType(ActivityType _activityType){
        ActivityType activityType;

        activityType = activityTypeRepository.findById(_activityType.getActivityTypeId())
                .orElse(activityTypeRepository.findActivityTypeByActivityTypeName(_activityType.getActivityTypeName()).
                        orElseThrow(() -> new ResourceNotFoundException("Activity type of a given id or given name doesn't exist")));

        return activityType;
    }

    private User getNewActivityCreator(User _user){
        User user;

        user = userRepository.findById(_user.getUserId()).
                orElseThrow(() -> new ResourceNotFoundException("User with a given id doesn't exist"));
        return user;
    }

    private Team getNewActivityTeam(Team _team){
        Team team;
        team = teamRepository.findById(_team.getTeamId()).orElseThrow(() ->
                new ResourceNotFoundException("The team of the user doesn't exist"));
        return team;
    }

    private Match getNewActivityMatch(ActivityType activityType, Activity activity){
        Match match;
        if(activityType.getActivityTypeName().equals(ActivityTypeEnum.MATCH.toString())) {
            match = new Match(activity);
            matchRepository.save(match);
        }else{
            match = null;
        }
        return match;
    }

    private Collection<ActivityStatus> getNewActivityActivityStatuses(Team team, int activityId) {
        Collection<ActivityStatus> activityStatuses = new ArrayList<>();

        List<User> players = userRepository.findAllByTeamTeamId(team.getTeamId());

        for(User player : players){
            activityStatuses.add(activityStatusRepository.save(new ActivityStatus(
                    userStatusRepository.findByStatusName(UserStatusesEnum.HAS_NOT_ANSWERED.toString()).getStatusId(),
                    player.getUserId(),
                    activityId
            )));
        }
        return activityStatuses;
    }

    public ResponseEntity<Activity> createActivity (Activity activity){
        User user;
        ActivityType activityType;
        Location location;
        Reservation reservation;
        Team team;
        Match match;
        Collection<ActivityStatus> activityStatuses;

        //Get a creator of the activity
        user = getNewActivityCreator(activity.getCreator());

        //Get the activity Type
        activityType = getNewActivityActivityType(activity.getActivityType());

        //Create a new reservation
        location = getNewActivityLocation(activity.getReservation().getLocation());

        //Get reservation object
        reservation = getNewActivityReservation(location, activity.getReservation());
        System.out.println(reservation.toString());
        //Get the team of the activity
        team = getNewActivityTeam(activity.getTeam());

        //TODO Validate whether the location is free within the given time frames before saving it to the database

        activity.setActivityType(activityType);
        activity.setReservation(reservation);
        activity.setCreator(user);
        activity.setTeam(team);
        activity.setIsCancelled(false);
        activity.setUserResponsibilities(null);
        activity.setMatch(null);

        try {
            Activity _activity = activityRepository.save(activity);

            //Set a match object if activity Type match is present
            match = getNewActivityMatch(activityType, activity);

            if(match != null) {
                _activity.setMatch(match);
                Activity activityWithMatch = activityRepository.save(_activity);
                return new ResponseEntity<>(activityWithMatch, HttpStatus.CREATED);
            }
            //TODO return activity statuses - get users of a team and set everyone to - not answered yet - create enum for that.
            //  activityStatuses = getNewActivityActivityStatuses(team, activity.getActivityId());
            //  _activity.setActivityStatuses(activityStatuses);

            return new ResponseEntity<>(_activity, HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
