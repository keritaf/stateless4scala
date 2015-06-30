package stateless4scala

/**
 * State transition
 *
 * @param source      The state transitioned from
 * @param destination The state transitioned to
 * @param trigger     The trigger that caused the transition
 */
case class Transition[S, T](val source: S, val destination: S, val trigger: T) {
  /**
   * True if the transition is a re-entry, i.e. the identity transition
   *
   * @return True if the transition is a re-entry
   */
  def isReentry: Boolean = source.equals(destination)
}
