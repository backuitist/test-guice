import com.google.inject.{Guice, AbstractModule}
import javax.inject.Inject

object InjectInOverloadedConstructor {

  trait A
  trait B

  class Aimpl extends A
  class Bimpl extends B

  class NeedBoth private[InjectInOverloadedConstructor](a: A, b : B) {

    @Inject()
    def this(a: A) = this(a = a, b = new Bimpl)

    override def toString = s"NeedBoth[$a, $b]"
  }

  object Module extends AbstractModule {
    def configure(): Unit = {
      bind(classOf[A]).to(classOf[Aimpl])
    }
  }

  def main(args: Array[String]) {
    val guice = Guice.createInjector(Module)
    println( guice.getInstance(classOf[NeedBoth]) )
  }
}
