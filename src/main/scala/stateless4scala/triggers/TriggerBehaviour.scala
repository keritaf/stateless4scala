package stateless4scala.triggers

import scala.reflect.runtime.universe._

import stateless4scala.OutVar

abstract class TriggerBehaviour[S, T](val trigger: T, guard: => Boolean) {

  def isGuardConditionMet: Boolean = guard

  def resultsInTransitionFrom(source: S, args: Seq[(Type, Any)], dest: OutVar[S]): Boolean
}
