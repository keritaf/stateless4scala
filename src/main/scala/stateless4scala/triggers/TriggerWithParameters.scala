package stateless4scala.triggers

import scala.reflect.runtime.universe._

import stateless4scala.conversion.ParameterConversion

/**
 * Create a configured trigger
 *
 * @param underlyingTrigger Trigger represented by this trigger configuration
 * @param argumentTypes     The argument types expected by the trigger
 */
abstract class TriggerWithParameters[TState, TTrigger](val underlyingTrigger: TTrigger, private val expectedTypes: Type*) {
  require(expectedTypes != null, "expectedTypes is null")

  def validateParameters(args: (Type, Any)*) = {
    ParameterConversion.validate(args, expectedTypes)
  }
}

class TriggerWithParameters1[-TArg0: TypeTag, TState, TTrigger](underlyingTrigger: TTrigger)
    extends TriggerWithParameters[TState, TTrigger](underlyingTrigger, typeOf[TArg0])

class TriggerWithParameters2[-TArg0: TypeTag, -TArg1: TypeTag, TState, TTrigger](underlyingTrigger: TTrigger)
  extends TriggerWithParameters[TState, TTrigger](underlyingTrigger, typeOf[TArg0], typeOf[TArg1])

class TriggerWithParameters3[-TArg0: TypeTag, -TArg1: TypeTag, -TArg2: TypeTag, TState, TTrigger](underlyingTrigger: TTrigger)
  extends TriggerWithParameters[TState, TTrigger](underlyingTrigger, typeOf[TArg0], typeOf[TArg1], typeOf[TArg2])

class TriggerWithParameters4[-TArg0: TypeTag, -TArg1: TypeTag, -TArg2: TypeTag, -TArg3: TypeTag, TState, TTrigger](underlyingTrigger: TTrigger)
  extends TriggerWithParameters[TState, TTrigger](underlyingTrigger, typeOf[TArg0], typeOf[TArg1], typeOf[TArg2], typeOf[TArg3])
