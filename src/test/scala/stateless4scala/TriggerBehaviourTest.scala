package stateless4scala

import org.junit.Assert._
import org.junit.Test

import stateless4scala.triggers.TransitioningTriggerBehaviour
import Fixtures._

class TriggerBehaviourTest {

  @Test
  def ExposesCorrectUnderlyingTrigger(): Unit = {
    val transitioning = new TransitioningTriggerBehaviour[State, Trigger](Trigger.X, State.C, true)
    assertEquals(Trigger.X, transitioning.trigger)
  }

  @Test
  def WhenGuardConditionFalse_IsGuardConditionMetIsFalse(): Unit = {
    val transitioning = new TransitioningTriggerBehaviour[State, Trigger](Trigger.X, State.C, false)
    assertFalse(transitioning.isGuardConditionMet)
  }

  @Test
  def WhenGuardConditionTrue_IsGuardConditionMetIsTrue(): Unit = {
    val transitioning = new TransitioningTriggerBehaviour[State, Trigger](Trigger.X, State.C, true)
    assertTrue(transitioning.isGuardConditionMet)
  }
}
