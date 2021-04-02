(define (problem pb_logistics)
  (:domain logistics)

  (:objects
     plane - airplane
     truck - truck
     cdg lhr - airport
     south north - location
     paris london - city
     p1 p2 - package)

  (:init (incity cdg paris)
         (incity lhr london)
         (incity north paris)
         (incity south paris)
         (at plane lhr)
         (at truck cdg)
         (at p1 lhr)
         (at p2 lhr)
  )

  (:goal (and (at p1 north) (at p2 south) ))
)
