What is Distributed System?
It is composed of independent machines that are autonoums
Machines can run concourrently and fail without affecting others.


One of the main purpose of kubernetes cluster is to deploy and run
Also it supports zero down-time rolling upgrade using technique 
green/blue and canary deployments.

Kubernetes can schedule containers, that is PODS
that contain one or more co-located container , to the 
available nodes in cluster.

Kub can monitor the health of running container, K assumes
that container implement a livennes probe.

If liveness probe reports an unhealthy container , K
would restart the container.

To optimize the use of avilable HW resources,
conatiners can be configured with `quotas`. The amount of resources a conatiner need during start-up


Yuou should not mix-match (synchronous and Asynchrous code)
Distributed System
===============================

K provides `Service Discovery` of the running Pods and their containers.
K `Service` objects can be defined for service discovery

