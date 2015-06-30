package stateless4scala

import scala.reflect.runtime.universe._

import org.junit._
import org.junit.Assert._

import Fixtures._
import stateless4scala.triggers._

class TriggerWithParametersTest {

  @Test
  def DescribesUnderlyingTrigger(): Unit = {
    val twp = new TriggerWithParameters1[String, State, Trigger](Trigger.X)
    assertEquals(Trigger.X, twp.underlyingTrigger)
  }

  @Test
  def ParametersOfCorrectTypeAreAccepted(): Unit = {
    val twp = new TriggerWithParameters1[String, State, Trigger](Trigger.X)
    twp.validateParameters((typeOf[String], "arg"))
  }

  @Test(expected = classOf[IllegalStateException])
  def IncompatibleParametersAreNotValid(): Unit = {
    val twp = new TriggerWithParameters1[String, State, Trigger](Trigger.X)
    twp.validateParameters((typeOf[Int], 123))
  }

  @Test(expected = classOf[IllegalStateException])
  def TooFewParametersDetected(): Unit = {
    val twp = new TriggerWithParameters2[String, String, State, Trigger](Trigger.X)
    twp.validateParameters((typeOf[String], "a"))
  }

  @Test(expected = classOf[IllegalStateException])
  def TooManyParametersDetected(): Unit = {
    val twp = new TriggerWithParameters2[String, String, State, Trigger](Trigger.X)
    twp.validateParameters((typeOf[String], "a"), (typeOf[String], "b"), (typeOf[String], "c"))
  }
}
