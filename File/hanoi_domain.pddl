;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; hanoi towers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (domain HANOI)
  (:requirements :strips :typing)
  (:types object tower)
  (:predicates
	       (clear ?x - object)
	       (handempty)
	       (holding ?x - object)
         (smaller ?x - object ?y - object)
         (on ?x - object ?y - object)
         (ontower ?x - object ?y - tower)
	       )

  (:action unstack
	     :parameters (?x - object ?y - object)
	     :precondition (and (clear ?x) (on ?x ?y) (handempty))
	     :effect
	     (and (not (on ?x ?y))
		   (not (clear ?x))
		   (not (handempty))
		   (holding ?x)
       (clear ?y)))

  (:action stack
	     :parameters (?x - object ?y - object)
	     :precondition (and (holding ?x) (clear ?y) (smaller ?x ?y))
	     :effect
	     (and (not (holding ?x))
       (not (clear ?y))
		   (clear ?x)
		   (handempty)
       (on ?x ?y)))

  (:action put-down
	     :parameters (?x - object ?y - tower)
	     :precondition (and (holding ?x) (clear ?y))
	     :effect
	     (and (not (holding ?x))
		   (not (clear ?y))
		   (clear ?x)
		   (handempty)
		   (ontower ?x ?y)))

  (:action pick-up
	     :parameters (?x - object ?y - tower)
	     :precondition (and (ontower ?x ?y) (clear ?x) (handempty))
	     :effect
	     (and (holding ?x)
		   (clear ?y)
		   (not (clear ?x))
		   (not (handempty))
		   (not (ontower ?x ?y)))))
