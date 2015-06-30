package stateless4scala

import org.junit._
import org.junit.Assert._

import Fixtures._

class DynamicTriggerTest {

  @Test
  def DestinationStateIsDynamic(): Unit = {
    val config = new StateMachineConfig[State, Trigger]
    config.configure(State.A).permitDynamic(Trigger.X, State.B)

    val sm = new StateMachine[State, Trigger](State.A, config)
    sm.fire(Trigger.X)

    assertEquals(State.B, sm.currentState)
  }

  @Test
  def DestinationStateIsCalculatedBasedOnTriggerParameters(): Unit = {
    val config = new StateMachineConfig[State, Trigger]()
    val trigger = config.setTriggerParameters[Integer](Trigger.X)

    config.configure(State.A).permitDynamic(trigger, (i: Integer) => {
      if (i == 1) State.B else State.C
    })

    val sm = new StateMachine[State, Trigger](State.A, config)
    sm.fire[Integer](trigger, 1)
    assertEquals(State.B, sm.currentState)
  }
}
