(define (problem DLOG-3-3-5)
	(:domain driverlog)
	(:objects
	driver1 - driver
	driver2 - driver
	driver3 - driver
	vehicle1 - vehicle
	vehicle2 - vehicle
	vehicle3 - vehicle
	package1 - obj
	package2 - obj
	package3 - obj
	package4 - obj
	package5 - obj
	s0 - location
	s1 - location
	s2 - location
	p1-2 - location
	p2-0 - location
	)
	(:init
	(at driver1 s2)
	(at driver2 s2)
	(at driver3 s1)
	(at vehicle1 s0)
	(empty vehicle1)
	(at vehicle2 s1)
	(empty vehicle2)
	(at vehicle3 s1)
	(empty vehicle3)
	(at package1 s1)
	(at package2 s1)
	(at package3 s0)
	(at package4 s1)
	(at package5 s1)
	(path s1 p1-2)
	(path p1-2 s1)
	(path s2 p1-2)
	(path p1-2 s2)
	(path s2 p2-0)
	(path p2-0 s2)
	(path s0 p2-0)
	(path p2-0 s0)
	(link s0 s1)
	(link s1 s0)
	(link s0 s2)
	(link s2 s0)
	(link s2 s1)
	(link s1 s2)
)
	(:goal (and
	(at driver1 s1)
	(at driver2 s0)
	(at vehicle1 s0)
	(at vehicle2 s0)
	(at vehicle3 s2)
	(at package1 s2)
	(at package2 s1)
	(at package3 s0)
	(at package4 s1)
	(at package5 s1)
	))


)
