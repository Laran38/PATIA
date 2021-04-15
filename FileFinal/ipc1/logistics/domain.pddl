;; logistics domain Typed version.
;;

(define (domain logistics)
  (:requirements :strips :typing)
  (:types vehicle
          airplane - vehicle
          package
          vehicle - physobj
          airport
          location - place
          city
          place
          physobj - object)

  (:predicates 	(in-city ?loc - place ?city - city)
		(at ?obj - physobj ?loc - place)
		(in ?pkg - package ?veh - vehicle))

(:action LOAD-vehicle
   :parameters    (?pkg - package ?vehicle - vehicle ?loc - place)
   :precondition  (and (at ?vehicle ?loc) (at ?pkg ?loc))
   :effect        (and (not (at ?pkg ?loc)) (in ?pkg ?vehicle)))

(:action LOAD-AIRPLANE
  :parameters   (?pkg - package ?airplane - airplane ?loc - place)
  :precondition (and (at ?pkg ?loc) (at ?airplane ?loc))
  :effect       (and (not (at ?pkg ?loc)) (in ?pkg ?airplane)))

(:action UNLOAD-vehicle
  :parameters   (?pkg - package ?vehicle - vehicle ?loc - place)
  :precondition (and (at ?vehicle ?loc) (in ?pkg ?vehicle))
  :effect       (and (not (in ?pkg ?vehicle)) (at ?pkg ?loc)))

(:action UNLOAD-AIRPLANE
  :parameters    (?pkg - package ?airplane - airplane ?loc - place)
  :precondition  (and (in ?pkg ?airplane) (at ?airplane ?loc))
  :effect        (and (not (in ?pkg ?airplane)) (at ?pkg ?loc)))

(:action DRIVE-vehicle
  :parameters (?vehicle - vehicle ?loc-from - place ?loc-to - place ?city - city)
  :precondition
   (and (at ?vehicle ?loc-from) (in-city ?loc-from ?city) (in-city ?loc-to ?city))
  :effect
   (and (not (at ?vehicle ?loc-from)) (at ?vehicle ?loc-to)))

(:action FLY-AIRPLANE
  :parameters (?airplane - airplane ?loc-from - airport ?loc-to - airport)
  :precondition
   (at ?airplane ?loc-from)
  :effect
   (and (not (at ?airplane ?loc-from)) (at ?airplane ?loc-to)))
)
