package stateless4scala

import org.junit._
import org.junit.Assert._

import Fixtures._
import stateless4scala.triggers.IgnoredTriggerBehaviour

class IgnoredTriggerBehaviourTest {

  @Test
  def StateRemainsUnchanged() {
    val ignored = new IgnoredTriggerBehaviour[State, Trigger](Trigger.X, true)
    assertFalse(ignored.resultsInTransitionFrom(State.B, Seq(), new OutVar[State]()))
  }

  @Test
  def ExposesCorrectUnderlyingTrigger() {
    val ignored = new IgnoredTriggerBehaviour[State, Trigger](Trigger.X, true)
    assertEquals(Trigger.X, ignored.trigger)
  }

  @Test
  def WhenGuardConditionFalse_IsGuardConditionMetIsFalse() {
    val ignored = new IgnoredTriggerBehaviour[State, Trigger](Trigger.X, false)
    assertFalse(ignored.isGuardConditionMet)
  }

  @Test
  def WhenGuardConditionTrue_IsGuardConditionMetIsTrue() {
    val ignored = new IgnoredTriggerBehaviour[State, Trigger](Trigger.X, true)
    assertTrue(ignored.isGuardConditionMet)
  }
}
