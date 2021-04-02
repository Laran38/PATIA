(define (domain MOVE)
  (:requirements :strips :typing :negative-preconditions)
  (:types robot lieu)
  (:predicates
          (at ?x - robot ?l - lieu)
)

 (:action move
	     :parameters (?x - robot ?f -lieu ?t - lieu)
       :precondition (and
              (not (at ?x ?t))
              (at ?x ?f)
       )
	     :effect (and
           (not (at ?x ?f))
           (at ?x ?t)
       )
)
)
