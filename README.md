# Project Reactor Essentials

## Reactive Programming

<p> Reactive programming describes a design paradigm that relies on asynchronous programming logic to handle real-time updates to otherwise static content. It provides an efficient means -- the use of automated data streams -- to handle data updates to content whenever a user makes an inquiry. </p>

What is Reactive programming? 

- Reactive programming is about the concepts not implementation
- Event driven
- Data Streams... Data Stream everywhere
- asynchronous
- Non-Blocking
- Backpressure
- Funcitional/Declarative programming

Reactive Streams API Components

> The API consists of the following components that are required to be provided by reactive Stream implementation:

- publisher
- subscriber
- subscription
- processor

 
 /**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-Blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * 1. publisher sends all the objects requested.
 * 2. publisher sends all the objects it has. (onComplete) subscriber and subscriptions will be canceled.
 * 3. there is on error. (onError) -> subscriber and subscriptions will be canceled.
 */
