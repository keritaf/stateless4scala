# stateless4scala - lightweight state machine for scala

stateless4scala is a port of [stateless4j](https://github.com/oxo42/stateless4j) for scala

stateless4j is a port of [stateless](https://github.com/nblumhardt/stateless) for java

Maven
=====
```xml
<dependency>
    <groupId>stateless4scala</groupId>
    <artifactId>stateless4scala</artifactId>
    <version>1.0.0</version>
</dependency>
```

Introduction
============
Create state machines and lightweight state machine-based workflows __directly in java code__.

```scala
val phoneCallConfig = new StateMachineConfig[State, Trigger]

phoneCallConfig.configure(State.OffHook)
  .permit(Trigger.CallDialed, State.Ringing)

phoneCallConfig.configure(State.Ringing)
  .permit(Trigger.HungUp, State.OffHook)
  .permit(Trigger.CallConnected, State.Connected)

phoneCallConfig.configure(State.Connected)
  .onEntry(startCallTimer)
  .onExit(stopCallTimer)
  .permit(Trigger.LeftMessage, State.OffHook)
  .permit(Trigger.HungUp, State.OffHook)
  .permit(Trigger.PlacedOnHold, State.OnHold)

// ...

val phoneCall = new StateMachine[State, Trigger](State.OffHook, phoneCallConfig)

phoneCall.fire(Trigger.CallDialed)
assertEquals(State.Ringing, phoneCall.getState())
```

Features
========
Most standard state machine constructs are supported:

* Generic support for states and triggers of any java/scala type (numbers, strings, enums, etc.)
* Hierarchical states
* Entry/exit events for states
* Guard clauses to support conditional transitions
* Introspection


Some useful extensions are also provided:
* Parameterised triggers
* Reentrant states


Hierarchical States
===================
In the example below, the `OnHold` state is a substate of the `Connected` state. This means that an `OnHold` call is
still connected.

```scala
phoneCall.configure(State.OnHold)
  .substateOf(State.Connected)
  .permit(Trigger.TakenOffHold, State.Connected)
  .permit(Trigger.HungUp, State.OffHook)
  .permit(Trigger.PhoneHurledAgainstWall, State.PhoneDestroyed)
```

In addition to the `StateMachine.getState()` property, which will report the precise current state, an `isInState(State)`
method is provided. `isInState(State)` will take substates into account, so that if the example above was in the
`OnHold` state, `isInState(State.Connected)` would also evaluate to `true`.

Entry/Exit Events
=================
In the example, the `startCallTimer()` method will be executed when a call is connected. The `stopCallTimer()` will be
executed when call completes (by either hanging up or hurling the phone against the wall.)

The call can move between the `Connected` and `OnHold` states without the `startCallTimer(`) and `stopCallTimer()`
methods being called repeatedly because the `OnHold` state is a substate of the `Connected` state.

Entry/Exit event handlers can be supplied with a parameter of type `Transition` that describes the trigger,
source and destination states.

License
=======
Apache 2.0 License