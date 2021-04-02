(define (domain FENETRE)
  (:requirements :strips :typing :negative-preconditions)
  (:types humain piece fenetre)
  (:predicates
          (at ?piece ?humain)
          (tomber ?humain)
          (ouverte ?fenetre ?piece)
    )

 (:action ouvreFenetre
      :parameters (?h - humain ?maison - piece ?fenetre - fenetre)
      :precondition (and
             (not (tomber ?h))
             (at ?maison ?h)
             (not (ouverte ?fenetre ?maison))
      )
      :effect
            (ouverte ?fenetre ?maison)

      )

 (:action saute
	     :parameters (?h - humain ?maison - piece ?sol - piece ?fenetre - fenetre)
       :precondition (and
              (not (tomber ?h))
              (at ?maison ?h)
              (ouverte ?fenetre ?maison)
       )

	     :effect (and
           (tomber ?h)
           (not (at ?maison ?h))
           (at ?sol ?h)
        )
 )

 (:action releve
     :parameters (?h - humain)
     :precondition (tomber ?h)

     :effect
         (not (tomber ?h))
 )
)
