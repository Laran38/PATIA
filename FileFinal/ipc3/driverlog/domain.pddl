(define (domain driverlog)
  (:requirements :typing)
  (:types         location locatable - object
		driver vehicle obj - locatable

  )
  (:predicates
		(at ?obj - locatable ?loc - location)
		(in ?obj1 - obj ?obj - vehicle)
		(driving ?d - driver ?v - vehicle)
		(link ?x ?y - location) (path ?x ?y - location)
		(empty ?v - vehicle)
)


(:action LOAD-vehicle
  :parameters
   (?obj - obj
    ?vehicle - vehicle
    ?loc - location)
  :precondition
   (and (at ?vehicle ?loc) (at ?obj ?loc))
  :effect
   (and (not (at ?obj ?loc)) (in ?obj ?vehicle)))

(:action UNLOAD-vehicle
  :parameters
   (?obj - obj
    ?vehicle - vehicle
    ?loc - location)
  :precondition
   (and (at ?vehicle ?loc) (in ?obj ?vehicle))
  :effect
   (and (not (in ?obj ?vehicle)) (at ?obj ?loc)))

(:action BOARD-vehicle
  :parameters
   (?driver - driver
    ?vehicle - vehicle
    ?loc - location)
  :precondition
   (and (at ?vehicle ?loc) (at ?driver ?loc) (empty ?vehicle))
  :effect
   (and (not (at ?driver ?loc)) (driving ?driver ?vehicle) (not (empty ?vehicle))))

(:action DISEMBARK-vehicle
  :parameters
   (?driver - driver
    ?vehicle - vehicle
    ?loc - location)
  :precondition
   (and (at ?vehicle ?loc) (driving ?driver ?vehicle))
  :effect
   (and (not (driving ?driver ?vehicle)) (at ?driver ?loc) (empty ?vehicle)))

(:action DRIVE-vehicle
  :parameters
   (?vehicle - vehicle
    ?loc-from - location
    ?loc-to - location
    ?driver - driver)
  :precondition
   (and (at ?vehicle ?loc-from)
   (driving ?driver ?vehicle) (link ?loc-from ?loc-to))
  :effect
   (and (not (at ?vehicle ?loc-from)) (at ?vehicle ?loc-to)))

(:action WALK
  :parameters
   (?driver - driver
    ?loc-from - location
    ?loc-to - location)
  :precondition
   (and (at ?driver ?loc-from) (path ?loc-from ?loc-to))
  :effect
   (and (not (at ?driver ?loc-from)) (at ?driver ?loc-to)))


)
