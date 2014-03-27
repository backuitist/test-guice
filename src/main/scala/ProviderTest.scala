import com.google.inject.{Provider, Guice, Provides, AbstractModule}
import javax.inject.Inject
import scala.util.control.NonFatal

object ProviderTest {

  trait ServiceA
  class ServiceAImpl extends ServiceA

  class NeedServiceA @Inject()(serviceA: Provider[ServiceA]) {
    def useServiceA() {
      try {
        println( "Built a serviceA : " + serviceA.get() )
      } catch {
        case NonFatal(e) =>
          println("Failed to build serviceA")
          e.printStackTrace()
      }
    }
  }

  object ModuleA extends AbstractModule {
    def configure() {
    }

    var failA = true

    @Provides
    def serviceA() : ServiceA = {
      if( failA ) sys.error("failing A")
      new ServiceAImpl
    }
  }

  def main(args: Array[String]) {
    val guice = Guice.createInjector(ModuleA)
    val nsA = guice.getInstance(classOf[NeedServiceA])
    nsA.useServiceA()
    ModuleA.failA = false
    nsA.useServiceA()
  }
}
