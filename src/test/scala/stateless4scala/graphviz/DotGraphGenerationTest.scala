package stateless4scala.graphviz

import java.io.ByteArrayOutputStream



import org.junit._
import org.junit.Assert._

import scalax.io.Codec
import scalax.io.JavaConverters._
import scalax.io.Line
import scalax.io.Resource
import stateless4scala._
import stateless4scala.Fixtures._

class DotGraphGenerationTest {
    @Test
    def testGenerateSimpleGraph(): Unit = {
        val config = new StateMachineConfig[State, Trigger]()
        config.configure(State.A)
            .permit(Trigger.X, State.B)
            .permit(Trigger.Y, State.C)

        config.configure(State.B)
            .permit(Trigger.Y, State.C)

        config.configure(State.C)
            .permit(Trigger.X, State.A)

        val sm = new StateMachine[State, Trigger](State.A, config)

        val expected = Resource.fromInputStream(this.getClass.getResourceAsStream("/simpleGraph.txt")).lines(Line.Terminators.Auto, false)(Codec.UTF8).toArray
        val dotFile = new ByteArrayOutputStream()
        config.generateDotFileInto(Resource.fromOutputStream(dotFile))
        val actual = dotFile.toByteArray().asInput.lines(Line.Terminators.Auto, false)(Codec.UTF8).toArray

        assertEquals(expected.length, actual.length)
        for (i <- 0 until actual.length) yield { assertEquals(expected(i), actual(i)) }
    }
}
