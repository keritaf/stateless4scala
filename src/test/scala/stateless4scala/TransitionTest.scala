package stateless4scala

import org.junit._
import org.junit.Assert._

class TransitionTest {
  @Test
  def IdentityTransitionIsNotChange(): Unit = {
    val t = new Transition(1, 1, 0)
    assertTrue(t.isReentry)
  }

  @Test
  def TransitioningTransitionIsChange(): Unit = {
    val t = new Transition(1, 2, 0)
    assertFalse(t.isReentry)
  }
}
