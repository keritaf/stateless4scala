package stateless4scala

import org.junit.Test
import org.junit.Assert._

import Fixtures._

class StateMachineTest {
  val StateA = State.A
  val StateB = State.B
  val StateC = State.C
  val TriggerX = Trigger.X
  val TriggerY = Trigger.Y

  var fired = false
  var entryArgS: String = null
  val entryArgI: Int = 0

  @Test
  def CanUseReferenceTypeMarkers(): Unit = {
    RunSimpleTest(Seq[Any](StateA, StateB, StateC),
      Seq[Any](TriggerX, TriggerY))
  }

  @Test
  def CanUseValueTypeMarkers() {
    RunSimpleTest[State, Trigger](State.values.toSeq, Trigger.values.toSeq)
  }

  def RunSimpleTest[S, T](states: Seq[S], transitions: Seq[T]): Unit = {
    val a = states(0)
    val b = states(1)
    val x = transitions(0)

    val config = new StateMachineConfig[S, T]()

    config.configure(a).permit(x, b)

    val sm = new StateMachine[S, T](a, config)
    sm.fire(x)

    assertEquals(b, sm.currentState)
  }

  @Test
  def InitialStateIsCurrent(): Unit = {
    val initial = State.B
    val sm = new StateMachine[State, Trigger](initial, new StateMachineConfig[State, Trigger]())
    assertEquals(initial, sm.currentState)
  }

  @Test
  def SubstateIsIncludedInCurrentState() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.B).substateOf(State.C)

    val sm = new StateMachine[State, Trigger](State.B, config)

    assertEquals(State.B, sm.currentState)
    assertTrue(sm.isInState(State.C))
  }

  @Test
  def WhenInSubstate_TriggerIgnoredInSuperstate_RemainsInSubstate() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.B)
      .substateOf(State.C)

    config.configure(State.C)
      .ignore(Trigger.X)

    val sm = new StateMachine[State, Trigger](State.B, config)
    sm.fire(Trigger.X)

    assertEquals(State.B, sm.currentState)
  }

  @Test
  def PermittedTriggersIncludeSuperstatePermittedTriggers() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.A)
      .permit(Trigger.Z, State.B)

    config.configure(State.B)
      .substateOf(State.C)
      .permit(Trigger.X, State.A)

    config.configure(State.C)
      .permit(Trigger.Y, State.A)

    val sm = new StateMachine[State, Trigger](State.B, config)
    val permitted = sm.permittedTriggers

    assertTrue(permitted.contains(Trigger.X))
    assertTrue(permitted.contains(Trigger.Y))
    assertFalse(permitted.contains(Trigger.Z))
  }

  @Test
  def PermittedTriggersAreDistinctValues() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.B)
      .substateOf(State.C)
      .permit(Trigger.X, State.A)

    config.configure(State.C)
      .permit(Trigger.X, State.B)

    val sm = new StateMachine[State, Trigger](State.B, config)
    val permitted = sm.permittedTriggers

    assertEquals(1, permitted.size)
    assert(permitted.contains(Trigger.X))
  }

  @Test
  def AcceptedTriggersRespectGuards() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.B)
      .permitIf(Trigger.X, State.A, false)

    val sm = new StateMachine[State, Trigger](State.B, config)

    assertEquals(0, sm.permittedTriggers.size)
  }

  @Test
  def WhenDiscriminatedByGuard_ChoosesPermitedTransition() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.B)
      .permitIf(Trigger.X, State.A, false)
      .permitIf(Trigger.X, State.C, true)

    val sm = new StateMachine[State, Trigger](State.B, config)
    sm.fire(Trigger.X)

    assertEquals(State.C, sm.currentState)
  }

  private def setFired(): Unit = {
    this.fired = true
  }

  @Test
  def WhenTriggerIsIgnored_ActionsNotExecuted() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.B).onEntry(setFired).ignore(Trigger.X)

    fired = false

    val sm = new StateMachine[State, Trigger](State.B, config)
    sm.fire(Trigger.X)

    assertFalse(fired)
  }

  @Test
  def IfSelfTransitionPermited_ActionsFire() {
    val config = new StateMachineConfig[State, Trigger]()

    config.configure(State.B).onEntry(setFired).permitReentry(Trigger.X)

    fired = false

    val sm = new StateMachine[State, Trigger](State.B, config)
    sm.fire(Trigger.X)

    assertTrue(fired)
  }

  @Test(expected = classOf[IllegalStateException])
  def ImplicitReentryIsDisallowed() {
    val config = new StateMachineConfig[State, Trigger]()

    val sm = new StateMachine[State, Trigger](State.B, config)

    config.configure(State.B)
      .permit(Trigger.X, State.B)
  }

  @Test(expected = classOf[IllegalStateException])
  def TriggerParametersAreImmutableOnceSet(): Unit = {
    val config = new StateMachineConfig[State, Trigger]()

    val sm = new StateMachine[State, Trigger](State.B, config)

    config.setTriggerParameters[String, Int](Trigger.X)
    config.setTriggerParameters[String](Trigger.X)
  }
}
