import com.google.inject.{Provides, Provider, AbstractModule, Guice}
import javax.inject.Inject

object CyclicDependencyWithProvider {

  trait A
  class B

  class ProvideA @Inject()(bProvider: Provider[B]) extends Provider[A] {
    def get() : A = {
      new A {}
    }

    def start() {
      println("Using b : " + bProvider.get)
    }
  }

  object Module extends AbstractModule {
    override def configure() {
      bind(classOf[A]).toProvider(classOf[ProvideA])
    }

    @Provides
    def provideB(a : A) : B = {
      println("producing b using " + a)
      new B {}
    }
  }

  def main(args: Array[String]) {
    val guice = Guice.createInjector(Module)
    val a = guice.getInstance(classOf[A])
    guice.getInstance(classOf[ProvideA]).start()
    println("Got : " + a)
  }
}
