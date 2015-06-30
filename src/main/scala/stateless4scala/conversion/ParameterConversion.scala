package stateless4scala.conversion

import scala.reflect.runtime.universe._

object ParameterConversion {
  def validate(index: Int, arg: Any, argType: Type, expectedType: Type): Any = {
    if (!(argType <:< expectedType)) {
      throw new IllegalStateException(s"The argument in position $index is of type $argType but must be of type $expectedType.")
    }
  }

  def validate(args: Seq[(Type, Any)], expectedTypes: Seq[Type]) {
    require(args != null, "args is null")
    require(expectedTypes != null, "expectedTypes is null")

    if (args.length > expectedTypes.length) {
      throw new IllegalStateException(s"Too many parameters have been supplied. Expecting ${expectedTypes.length} but got ${args.length}.")
    }
    if (args.length < expectedTypes.length) {
      throw new IllegalStateException(s"Too few parameters have been supplied. Expecting ${expectedTypes.length} but got ${args.length}.")
    }

    args.zipWithIndex foreach {
      case ((tpe, arg), index) => validate(index, arg, tpe, expectedTypes(index))
    }
  }
}
