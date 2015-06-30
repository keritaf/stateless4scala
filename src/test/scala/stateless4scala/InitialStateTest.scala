package stateless4scala

import org.junit.Test
import org.junit.Assert._

import Fixtures._

class InitialStateTest {

  @Test
  def initialStateEntryActionMustNotExecuteIfExecutionDisabled(): Unit = {
    val initial = State.B
    var executed = false

    val config = new StateMachineConfig[State, Trigger]
    config.setInitialStateEntryActionEnabled(false)
    config.configure(initial).onEntry(executed = true)

    val sm = new StateMachine[State, Trigger](initial, config)

    assertEquals(initial, sm.currentState)
    assertFalse(executed)
  }

  @Test
  def initialStateEntryActionMustExecuteIfExecutionEnabled(): Unit = {
    val initial = State.B
    var executed = false

    val config = new StateMachineConfig[State, Trigger]
    config.setInitialStateEntryActionEnabled(true)
    config.configure(initial).onEntry(executed = true)

    val sm = new StateMachine[State, Trigger](initial, config)

    assertEquals(initial, sm.currentState)
    assertTrue(executed)
  }
}
