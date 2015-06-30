package stateless4scala

import scala.collection.mutable
import scala.reflect.runtime.universe._

import scalax.io.Output
import stateless4scala.triggers._

class StateMachineConfig[TState, TTrigger] {

  private val stateConfiguration = mutable.Map[TState, StateRepresentation[TState, TTrigger]]()
  private val triggerConfiguration = mutable.Map[TTrigger, TriggerWithParameters[TState, TTrigger]]()

  private var initialStateEntryActionEnabled = false

  /**
   * Return StateRepresentation for the specified state. May return null.
   *
   * @param state The state
   * @return StateRepresentation for the specified state, or null.
   */
  def getRepresentation(state: TState): Option[StateRepresentation[TState, TTrigger]] = {
    stateConfiguration.get(state)
  }

  /**
   * Return StateRepresentation for the specified state. Creates representation if it does not exist.
   *
   * @param state The state
   * @return StateRepresentation for the specified state.
   */
  private def getOrCreateRepresentation(state: TState): StateRepresentation[TState, TTrigger] = {
    stateConfiguration.getOrElseUpdate(state, new StateRepresentation[TState, TTrigger](state))
  }

  def getTriggerConfiguration(trigger: TTrigger): Option[TriggerWithParameters[TState, TTrigger]] = {
    triggerConfiguration.get(trigger)
  }

  /**
   * Begin configuration of the entry/exit actions and allowed transitions
   * when the state machine is in a particular state
   *
   * @param state The state to configure
   * @return A configuration object through which the state can be configured
   */
  def configure(state: TState): StateConfiguration[TState, TTrigger] =
    new StateConfiguration[TState, TTrigger](getOrCreateRepresentation(state), getOrCreateRepresentation)

  private def saveTriggerConfiguration(trigger: TriggerWithParameters[TState, TTrigger]): Unit = {
    if (triggerConfiguration.contains(trigger.underlyingTrigger)) {
      throw new IllegalStateException("Parameters for the trigger '" + trigger + "' have already been configured.")
    }

    triggerConfiguration.put(trigger.underlyingTrigger, trigger)
  }

  /**
   * Specify the arguments that must be supplied when a specific trigger is fired
   *
   * @param trigger The underlying trigger value
   * @param classe0 Class argument
   * @tparam TArg0 Type of the first trigger argument
   * @return An object that can be passed to the fire() method in order to fire the parameterized trigger
   */
  def setTriggerParameters[TArg0: TypeTag](trigger: TTrigger): TriggerWithParameters1[TArg0, TState, TTrigger] = {
    val configuration = new TriggerWithParameters1[TArg0, TState, TTrigger](trigger)
    saveTriggerConfiguration(configuration)
    return configuration
  }

  /**
   * Specify the arguments that must be supplied when a specific trigger is fired
   *
   * @param trigger The underlying trigger value
   * @tparam TArg0 Type of the first trigger argument
   * @tparam TArg1 Type of the second trigger argument
   * @return An object that can be passed to the fire() method in order to fire the parameterized trigger
   */
  def setTriggerParameters[TArg0: TypeTag, TArg1: TypeTag](trigger: TTrigger): TriggerWithParameters2[TArg0, TArg1, TState, TTrigger] = {
    val configuration = new TriggerWithParameters2[TArg0, TArg1, TState, TTrigger](trigger)
    saveTriggerConfiguration(configuration)
    return configuration
  }

  /**
   * Specify the arguments that must be supplied when a specific trigger is fired
   *
   * @param trigger The underlying trigger value
   * @tparam TArg0 Type of the first trigger argument
   * @tparam TArg1 Type of the second trigger argument
   * @tparam TArg2 Type of the third trigger argument
   * @return An object that can be passed to the fire() method in order to fire the parameterized trigger
   */
  def setTriggerParameters[TArg0: TypeTag, TArg1: TypeTag, TArg2: TypeTag](trigger: TTrigger): TriggerWithParameters3[TArg0, TArg1, TArg2, TState, TTrigger] = {
    val configuration = new TriggerWithParameters3[TArg0, TArg1, TArg2, TState, TTrigger](trigger)
    saveTriggerConfiguration(configuration)
    return configuration
  }

  /**
   * Returns true if initial state entry action must be executed on state machine creation
   */
  def isInitialStateEntryActionEnabled: Boolean = initialStateEntryActionEnabled

  /**
   * Enables or disables initial state entry action execution when state machine is created
   */
  def setInitialStateEntryActionEnabled(isEnabled: Boolean): Unit = {
    initialStateEntryActionEnabled = isEnabled
  }

  def generateDotFileInto(dotFile: Output): Unit = {
    case class Line(state: String, destination: String)

    val destination = new OutVar[TState]()
    val lines = mutable.ListBuffer[Line]()
    for (
      (state, stateRepresentation) <- stateConfiguration;
      triggerBehaviours <- stateRepresentation.triggerBehaviours.values;
      triggerBehaviour <- triggerBehaviours.filter(_.isInstanceOf[TransitioningTriggerBehaviour[TState, TTrigger]]).map(_.asInstanceOf[TransitioningTriggerBehaviour[TState, TTrigger]])
    ) yield {
      destination.value = null.asInstanceOf[TState]
      triggerBehaviour.resultsInTransitionFrom(null.asInstanceOf[TState], Seq(), destination)
      lines += Line(state.toString(), destination.toString())
    }

    for {
      processor <- dotFile.outputProcessor
      output = processor.asOutput
    } {
      output.write("digraph G {\n")
      lines
        .sortWith { case (Line(s1, d1), Line(s2, d2)) => s1 < s2 || (s1 == s2 && d1 < d2) }
        .foreach { x => output.write(s"    ${x.state} -> ${x.destination};\n") }
      output.write("}\n")
    }
  }
}
