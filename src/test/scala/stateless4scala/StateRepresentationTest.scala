package stateless4scala

import scala.collection.mutable.ListBuffer

import org.junit._
import org.junit.Assert._

import Fixtures._
import stateless4scala.triggers.IgnoredTriggerBehaviour

class StateRepresentationTest {
  var actualTransition: Transition[State, Trigger] = null
  var executed = false

  var order = 0
  var subOrder = 0
  var superOrder = 0

  @Test
  def UponEntering_EnteringActionsExecuted(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    val transition = new Transition[State, Trigger](State.A, State.B, Trigger.X)
    actualTransition = null
    stateRepresentation.addEntryAction((t: Transition[State, Trigger], arg: Seq[Any]) => actualTransition = t)
    stateRepresentation.enter(transition)
    assertEquals(transition, actualTransition)
  }

  @Test
  def UponLeaving_EnteringActionsNotExecuted(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    val transition = new Transition[State, Trigger](State.A, State.B, Trigger.X)
    actualTransition = null
    stateRepresentation.addEntryAction((t, _) => actualTransition = t)
    stateRepresentation.exit(transition)
    assertNull(actualTransition)
  }

  @Test
  def UponLeaving_LeavingActionsExecuted(): Unit = {

    val stateRepresentation = CreateRepresentation(State.A)
    val transition = new Transition[State, Trigger](State.A, State.B, Trigger.X)
    actualTransition = null
    stateRepresentation.addExitAction(actualTransition = _)
    stateRepresentation.exit(transition)
    assertEquals(transition, actualTransition)
  }

  @Test
  def UponEntering_LeavingActionsNotExecuted(): Unit = {
    val stateRepresentation = CreateRepresentation(State.A)
    val transition = new Transition[State, Trigger](State.A, State.B, Trigger.X)
    actualTransition = null
    stateRepresentation.addExitAction(actualTransition = _)
    stateRepresentation.enter(transition)
    assertNull(actualTransition)
  }

  @Test
  def IncludesUnderlyingState(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    assertTrue(stateRepresentation.includes(State.B))
  }

  @Test
  def DoesNotIncludeUnrelatedState(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    assertFalse(stateRepresentation.includes(State.C))
  }

  @Test
  def IncludesSubstate(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    stateRepresentation.addSubstate(CreateRepresentation(State.C))
    assertTrue(stateRepresentation.includes(State.C))
  }

  @Test
  def DoesNotIncludeSuperstate(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    stateRepresentation.superstate = Some(CreateRepresentation(State.C))
    assertFalse(stateRepresentation.includes(State.C))
  }

  @Test
  def IsIncludedInUnderlyingState(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    assertTrue(stateRepresentation.isIncludedIn(State.B))
  }

  @Test
  def IsNotIncludedInUnrelatedState(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    assertFalse(stateRepresentation.isIncludedIn(State.C))
  }

  @Test
  def IsNotIncludedInSubstate(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    stateRepresentation.addSubstate(CreateRepresentation(State.C))
    assertFalse(stateRepresentation.isIncludedIn(State.C))
  }

  @Test
  def IsIncludedInSuperstate(): Unit = {
    val stateRepresentation = CreateRepresentation(State.B)
    stateRepresentation.superstate = Some(CreateRepresentation(State.C))
    assertTrue(stateRepresentation.isIncludedIn(State.C))
  }

  @Test
  def WhenTransitioningFromSubToSuperstate_SubstateEntryActionsExecuted(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    sub.addEntryAction((_, _) => executed = true)
    val transition = new Transition[State, Trigger](superState.getUnderlyingState(), sub.getUnderlyingState(), Trigger.X)
    sub.enter(transition)
    assertTrue(executed)
  }

  @Test
  def WhenTransitioningFromSubToSuperstate_SubstateExitActionsExecuted(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    executed = false
    sub.addExitAction(_ => executed = true)
    val transition = new Transition[State, Trigger](sub.getUnderlyingState(), superState.getUnderlyingState(), Trigger.X)
    sub.exit(transition)
    assertTrue(executed)
  }

  @Test
  def WhenTransitioningToSuperFromSubstate_SuperEntryActionsNotExecuted(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    executed = false
    superState.addEntryAction((_, _) => executed = true)
    val transition = new Transition[State, Trigger](superState.getUnderlyingState(), sub.getUnderlyingState(), Trigger.X)
    superState.enter(transition)
    assertFalse(executed)
  }

  @Test
  def WhenTransitioningFromSuperToSubstate_SuperExitActionsNotExecuted(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    executed = false
    superState.addExitAction(_ => executed = true)
    val transition = new Transition[State, Trigger](superState.getUnderlyingState(), sub.getUnderlyingState(), Trigger.X)
    superState.exit(transition)
    assertFalse(executed)
  }

  @Test
  def WhenEnteringSubstate_SuperEntryActionsExecuted(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    executed = false
    superState.addEntryAction((_, _) => executed = true)
    val transition = new Transition[State, Trigger](State.C, sub.getUnderlyingState(), Trigger.X)
    sub.enter(transition)
    assertTrue(executed)
  }

  @Test
  def WhenLeavingSubstate_SuperExitActionsExecuted(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    executed = false
    superState.addExitAction(_ => executed = true)
    val transition = new Transition[State, Trigger](sub.getUnderlyingState(), State.C, Trigger.X)
    sub.exit(transition)
    assertTrue(executed)
  }

  @Test
  def EntryActionsExecuteInOrder(): Unit = {
    val actual = ListBuffer[Int]()

    val rep = CreateRepresentation(State.B)
    rep.addEntryAction((_, _) => actual.append(0))
    rep.addEntryAction((_, _) => actual.append(1))

    rep.enter(new Transition[State, Trigger](State.A, State.B, Trigger.X))

    assertEquals(2, actual.size)
    assertEquals(0, actual(0))
    assertEquals(1, actual(1))
  }

  @Test
  def ExitActionsExecuteInOrder(): Unit = {
    val actual = ListBuffer[Int]()

    val rep = CreateRepresentation(State.B)
    rep.addExitAction(_ => actual.append(0))
    rep.addExitAction(_ => actual.append(1))

    rep.exit(new Transition[State, Trigger](State.B, State.C, Trigger.X))

    assertEquals(2, actual.size)
    assertEquals(0, actual(0))
    assertEquals(1, actual(1))
  }

  @Test
  def WhenTransitionExists_TriggerCanBeFired(): Unit = {
    val rep = CreateRepresentation(State.B)
    assertFalse(rep.canHandle(Trigger.X))
  }

  @Test
  def WhenTransitionExistsButGuardConditionNotMet_TriggerCanBeFired(): Unit = {
    val rep = CreateRepresentation(State.B)
    rep.addTriggerBehaviour(new IgnoredTriggerBehaviour[State, Trigger](Trigger.X, false))
    assertFalse(rep.canHandle(Trigger.X))
  }

  @Test
  def WhenTransitionDoesNotExist_TriggerCannotBeFired(): Unit = {
    val rep = CreateRepresentation(State.B)
    rep.addTriggerBehaviour(new IgnoredTriggerBehaviour[State, Trigger](Trigger.X, true))
    assertTrue(rep.canHandle(Trigger.X))
  }

  @Test
  def WhenTransitionExistsInSupersate_TriggerCanBeFired(): Unit = {
    val rep = CreateRepresentation(State.B)
    rep.addTriggerBehaviour(new IgnoredTriggerBehaviour[State, Trigger](Trigger.X, true))
    val sub = CreateRepresentation(State.C)
    sub.superstate = Some(rep)
    rep.addSubstate(sub)
    assertTrue(sub.canHandle(Trigger.X))
  }

  @Test
  def WhenEnteringSubstate_SuperstateEntryActionsExecuteBeforeSubstate(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    order = 0
    subOrder = 0
    superOrder = 0
    superState.addEntryAction((_, _) => {
      superOrder = order
      order = order + 1
    })
    sub.addEntryAction((_, _) => {
      subOrder = order
      order = order + 1
    })
    val transition = new Transition[State, Trigger](State.C, sub.getUnderlyingState(), Trigger.X)
    sub.enter(transition)
    assertTrue(superOrder < subOrder)
  }

  @Test
  def WhenExitingSubstate_SubstateEntryActionsExecuteBeforeSuperstate(): Unit = {
    val superState = CreateRepresentation(State.A)
    val sub = CreateRepresentation(State.B)
    superState.addSubstate(sub)
    sub.superstate = Some(superState)

    order = 0
    subOrder = 0
    superOrder = 0

    superState.addExitAction(_ => {
      superOrder = order
      order = order + 1
    })
    sub.addExitAction(_ => {
      subOrder = order
      order = order + 1
    })
    val transition = new Transition[State, Trigger](sub.getUnderlyingState(), State.C, Trigger.X)
    sub.exit(transition)
    assertTrue(subOrder < superOrder)
  }

  private def CreateRepresentation(state: State): StateRepresentation[State, Trigger] = new StateRepresentation[State, Trigger](state)
}
