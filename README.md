# Haren

Haren is a scheduling framework for stream processing systems, described in detailed in the publication:

## Publication 

[Haren: A Framework for Ad-Hoc Thread Scheduling Policies for Data Streaming Applications](https://dl.acm.org/citation.cfm?id=3329505).

In modern Stream Processing Engines (SPEs), numerous diverse applications, which can differ in aspects 
such as cost, criticality or latency sensitivity, can co-exist in the same computing node. 
When these differences need to be considered to control the performance of each application, 
custom scheduling of operators to threads is of key importance 
(e.g., when a smart vehicle needs to ensure that safety-critical applications always have access 
to computational power, while other applications are given lower, variable priorities).

Many solutions have been proposed regarding schedulers that allocate threads to operators 
to optimize specific metrics (e.g., latency) but there is still lack of a tool that allows 
arbitrarily complex scheduling strategies to be seamlessly plugged on top of an SPE. 
We propose Haren to fill this gap. More specifically, we 
(1) formalize the thread scheduling problem in stream processing in a general way, allowing 
to define ad-hoc scheduling policies, (2) identify the bottlenecks and the opportunities 
of scheduling in stream processing, (3) distill a compact interface to connect Haren with SPEs,
 enabling rapid testing of various scheduling policies, (4) illustrate the usability of the 
 framework by integrating it into an actual SPE and (5) provide a thorough evaluation. 
 As we show, Haren makes it is possible to adapt the use of computational resources over time 
 to meet the goals of a variety of scheduling policies.



## Usage

Haren can be coupled with an SPE using the `Task` and `HarenScheduler` entities, where a Task is a
unit of execution (e.g., an operator) that will be scheduled by `HarenScheduler`.
The scheduling behavior is controlled by defining an appropriate 
`InterThreadSchedulingFunction`, which controls the assignment of a `Task` to processing threads and
an `IntraThreadSchedulingFunction`,
which controls the prioritization of tasks in each thread.


