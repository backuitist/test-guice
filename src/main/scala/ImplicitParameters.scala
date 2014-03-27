import com.google.inject.{AbstractModule, Guice}
import javax.inject.Inject

object ImplicitParameters {

  trait Clock {
    def time: Long
  }

  class Time extends Clock {
    def time = System.currentTimeMillis()
  }

  class ServiceA {
    def save(t: Long) {
      println("Saving at " + t)
    }
  }

  class NeedClock @Inject()(srvA: ServiceA)(implicit val clock: Clock) {
    def saveNow() { srvA.save(clock.time)}
  }

  object ModuleA extends AbstractModule {
    def configure() {
      bind(classOf[Clock]).to(classOf[Time])
    }
  }

  def main(args: Array[String]) {
    val guice = Guice.createInjector(ModuleA)
    val needClock = guice.getInstance(classOf[NeedClock])
    needClock.saveNow()
  }
}
