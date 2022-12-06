# Task
 
Implement service that assigns collectible cards to users.

## Domain model

System has configuration that consists of one album and many cards. Album contains cards.
Example:

Album "Animals"

    - Card "Dog"
    - Card "Cat"
    - Card "Eagle"

## Functional requirements

Service has to support the following functions:

- add a single card to a user's collection 
   A card can be assigned to user only once: additional copies of the same card have no effect on user's collection.
 
- send event to external systems when user has collected all cards in entire album

    Event should be sent only once.

- allow several external systems to subscribe to service to receive events
    Once a system subscribes, it will receive all subsequent events. It should not receive events generated before it subscribed.

## Technical aspects

Configuration is supplied at runtime via an instance of *ConfigurationProvider* interface. Implementing this interface is not required.

Domain objects of the same type (i.e. all Album and all Cards) have unique ids.
 
State of each user's card collection should be stored in memory.

Requests for adding cards will be called in a multithreaded environment.

## Solution requirements

You can implement *com.playtika.cards.service.DefaultCardAssigner* or design own component.

You can use existing classes and change them at will, provided that all requirements above are still met.

We are going to rate 3 categories:
1. Design
2. Unit testing technique
3. Concurrency aspects

*CardAssignerTest* already contains some end-to-end scenarios made to verify your code. 
Please, don't waste time on investigating them. 
However, a correct implementation is expected to pass all of them successfully.