(define (domain taquin)
  (:requirements :strips :typing)
  (:types tile position)
  (:constants free - tile)
  (:predicates (on ?x - position ?y - tile)
	       (permutable ?x - position ?y - position))

 (:action move
	     :parameters (?t - tile ?from - position ?to - position)
	     :precondition (and (on ?from ?t) (on ?to free) (permutable ?from ?to))
	     :effect
	     (and (on ?to ?t) (on ?from free) (not (on ?from ?t)) (not (on ?to free))))

  (:action noop
	     :parameters (?from - position ?to - position)
	     :precondition (and (permutable ?from ?to))
	     :effect
	     (and (permutable ?to ?from)))
)
