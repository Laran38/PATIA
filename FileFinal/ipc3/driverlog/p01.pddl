(define (problem DLOG-2-2-2)
	(:domain driverlog)
	(:objects
	driver1 - driver
	driver2 - driver
	vehicle1 - vehicle
	vehicle2 - vehicle
	package1 - obj
	package2 - obj
	s0 - location
	s1 - location
	s2 - location
	p1-0 - location
	p1-2 - location
	)
	(:init
	(at driver1 s2)
	(at driver2 s2)
	(at vehicle1 s0)
	(empty vehicle1)
	(at vehicle2 s0)
	(empty vehicle2)
	(at package1 s0)
	(at package2 s0)
	(path s1 p1-0)
	(path p1-0 s1)
	(path s0 p1-0)
	(path p1-0 s0)
	(path s1 p1-2)
	(path p1-2 s1)
	(path s2 p1-2)
	(path p1-2 s2)
	(link s0 s1)
	(link s1 s0)
	(link s0 s2)
	(link s2 s0)
	(link s2 s1)
	(link s1 s2)
)
	(:goal (and
	(at driver1 s1)
	(at vehicle1 s1)
	(at package1 s0)
	(at package2 s0)
	))


)
