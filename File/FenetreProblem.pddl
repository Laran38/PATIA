(define (problem FENETRE)
  (:domain FENETRE)
  (:objects maison sol - piece
            fenetre - fenetre
            theo - humain)
  (:init
      (at maison theo)
      (not (tomber theo))
      (not (at sol theo))
  )

  (:goal (and
      (at sol theo)
      (not (tomber theo))
      (not (at maison theo))
      (ouverte fenetre maison)
    )
  )
)
