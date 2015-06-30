package stateless4scala

import org.junit._
import org.junit.Assert._

import Fixtures._

object NonEnumTest {
    val StateA = "StateA"
    val StateB = "StateB"
    val StateC = "StateC"

    val TriggerX = "TriggerX"
    val TriggerY = "TriggerY"

}

class NonEnumTest {
  import NonEnumTest._


    @Test
    def CanUseReferenceTypeMarkers():Unit = {
        RunSimpleTest(
                Seq[String](StateA, StateB, StateC),
                Seq[String](TriggerX, TriggerY))
    }

    @Test
    def CanUseValueTypeMarkers():Unit = {
        RunSimpleTest(State.values.toSeq, Trigger.values.toSeq)
    }

    def RunSimpleTest[S,T](states: Seq[S], transitions:Seq[T]) {
        val a = states(0)
        val b = states(1)
        val x = transitions(0)

        val config = new StateMachineConfig[S,T]()
        config.configure(a).permit(x, b)

        val sm = new StateMachine[S,T](a, config)
        sm.fire(x)

        assertEquals(b, sm.currentState)
    }

}
