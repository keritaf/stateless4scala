package stateless4scala

import org.slf4j.ILoggerFactory
import org.slf4j.Logger

object Fixtures {
  object State extends Enumeration {
    val A, B, C = Value
  }
  object Trigger extends Enumeration {
    val X, Y, Z = Value
  }

  type State = State.Value
  type Trigger = Trigger.Value

  implicit val NoOpLoggerFactory: ILoggerFactory = new ILoggerFactory {
    def getLogger(name: String): Logger = new Logger {
      def debug(x$1: org.slf4j.Marker, x$2: String, x$3: Throwable): Unit = {}
      def debug(x$1: org.slf4j.Marker, x$2: String, x$3: Object*): Unit = {}
      def debug(x$1: org.slf4j.Marker, x$2: String, x$3: Any, x$4: Any): Unit = {}
      def debug(x$1: org.slf4j.Marker, x$2: String, x$3: Any): Unit = {}
      def debug(x$1: org.slf4j.Marker, x$2: String): Unit = {}
      def debug(x$1: String, x$2: Throwable): Unit = {}
      def debug(x$1: String, x$2: Object*): Unit = {}
      def debug(x$1: String, x$2: Any, x$3: Any): Unit = {}
      def debug(x$1: String, x$2: Any): Unit = {}
      def debug(x$1: String): Unit = {}
      def error(x$1: org.slf4j.Marker, x$2: String, x$3: Throwable): Unit = {}
      def error(x$1: org.slf4j.Marker, x$2: String, x$3: Object*): Unit = {}
      def error(x$1: org.slf4j.Marker, x$2: String, x$3: Any, x$4: Any): Unit = {}
      def error(x$1: org.slf4j.Marker, x$2: String, x$3: Any): Unit = {}
      def error(x$1: org.slf4j.Marker, x$2: String): Unit = {}
      def error(x$1: String, x$2: Throwable): Unit = {}
      def error(x$1: String, x$2: Object*): Unit = {}
      def error(x$1: String, x$2: Any, x$3: Any): Unit = {}
      def error(x$1: String, x$2: Any): Unit = {}
      def error(x$1: String): Unit = {}
      def getName(): String = name
      def info(x$1: org.slf4j.Marker, x$2: String, x$3: Throwable): Unit = {}
      def info(x$1: org.slf4j.Marker, x$2: String, x$3: Object*): Unit = {}
      def info(x$1: org.slf4j.Marker, x$2: String, x$3: Any, x$4: Any): Unit = {}
      def info(x$1: org.slf4j.Marker, x$2: String, x$3: Any): Unit = {}
      def info(x$1: org.slf4j.Marker, x$2: String): Unit = {}
      def info(x$1: String, x$2: Throwable): Unit = {}
      def info(x$1: String, x$2: Object*): Unit = {}
      def info(x$1: String, x$2: Any, x$3: Any): Unit = {}
      def info(x$1: String, x$2: Any): Unit = {}
      def info(x$1: String): Unit = {}
      def isDebugEnabled(x$1: org.slf4j.Marker): Boolean = true
      def isDebugEnabled(): Boolean = true
      def isErrorEnabled(x$1: org.slf4j.Marker): Boolean = true
      def isErrorEnabled(): Boolean = true
      def isInfoEnabled(x$1: org.slf4j.Marker): Boolean = true
      def isInfoEnabled(): Boolean = true
      def isTraceEnabled(x$1: org.slf4j.Marker): Boolean = true
      def isTraceEnabled(): Boolean = true
      def isWarnEnabled(x$1: org.slf4j.Marker): Boolean = true
      def isWarnEnabled(): Boolean = true
      def trace(x$1: org.slf4j.Marker, x$2: String, x$3: Throwable): Unit = {}
      def trace(x$1: org.slf4j.Marker, x$2: String, x$3: Object*): Unit = {}
      def trace(x$1: org.slf4j.Marker, x$2: String, x$3: Any, x$4: Any): Unit = {}
      def trace(x$1: org.slf4j.Marker, x$2: String, x$3: Any): Unit = {}
      def trace(x$1: org.slf4j.Marker, x$2: String): Unit = {}
      def trace(x$1: String, x$2: Throwable): Unit = {}
      def trace(x$1: String, x$2: Object*): Unit = {}
      def trace(x$1: String, x$2: Any, x$3: Any): Unit = {}
      def trace(x$1: String, x$2: Any): Unit = {}
      def trace(x$1: String): Unit = {}
      def warn(x$1: org.slf4j.Marker, x$2: String, x$3: Throwable): Unit = {}
      def warn(x$1: org.slf4j.Marker, x$2: String, x$3: Object*): Unit = {}
      def warn(x$1: org.slf4j.Marker, x$2: String, x$3: Any, x$4: Any): Unit = {}
      def warn(x$1: org.slf4j.Marker, x$2: String, x$3: Any): Unit = {}
      def warn(x$1: org.slf4j.Marker, x$2: String): Unit = {}
      def warn(x$1: String, x$2: Throwable): Unit = {}
      def warn(x$1: String, x$2: Any, x$3: Any): Unit = {}
      def warn(x$1: String, x$2: Object*): Unit = {}
      def warn(x$1: String, x$2: Any): Unit = {}
      def warn(x$1: String): Unit = {}
    }
  }
}
