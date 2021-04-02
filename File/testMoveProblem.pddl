(define (problem MOVE)
  (:domain MOVE)
  (:objects l1 l2 - lieu
            r1 - robot)
  (:init
      (at r1 l1)
  )
  (:goal
  (at r1 l2)
  )
)
