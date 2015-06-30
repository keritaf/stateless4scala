package stateless4scala

import org.junit._
import org.junit.Assert._

import Fixtures._
import stateless4scala.triggers.TransitioningTriggerBehaviour

class TransitioningTriggerBehaviourTest {
  @Test
  def TransitionsToDestinationState(): Unit = {
    val transitioning = new TransitioningTriggerBehaviour[State, Trigger](Trigger.X, State.C, true)
    val destination = new OutVar[State]()
    assertTrue(transitioning.resultsInTransitionFrom(State.B, Seq(), destination))
    assertEquals(State.C, destination.value)
  }
}
