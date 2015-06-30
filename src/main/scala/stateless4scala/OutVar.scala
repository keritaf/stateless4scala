package stateless4scala

class OutVar[T] {

  private var obj: T = _

  def value: T = obj

  def value_=(v: T): Unit = {
    this.obj = v
  }

  override def toString: String = String.valueOf(obj)
}
