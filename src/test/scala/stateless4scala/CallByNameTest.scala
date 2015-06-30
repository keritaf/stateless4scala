package stateless4scala

import org.junit._
import org.junit.Assert._

import Fixtures._

class CallByNameTest {
  @Test
  def OnExitParameterIsCalledOnlyOnce() {
    val config = new StateMachineConfig[State, Trigger]()

    var fireCount = 0
    def fire(): Unit = { fireCount += 1 }

    config.configure(State.A).onExit(fire).permit(Trigger.X, State.B)

    val sm = new StateMachine[State, Trigger](State.A, config)
    sm.fire(Trigger.X)

    assertEquals(1, fireCount)
  }

  @Test
  def OnEntryParameterIsCalledOnlyOnce() {
    val config = new StateMachineConfig[State, Trigger]()

    var fireCount = 0
    def fire(): Unit = { fireCount += 1 }

    config.configure(State.A).permit(Trigger.X, State.B)
    config.configure(State.B).onEntry(fire)

    val sm = new StateMachine[State, Trigger](State.A, config)
    sm.fire(Trigger.X)

    assertEquals(1, fireCount)
  }
}
