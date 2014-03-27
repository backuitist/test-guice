import com.google.inject._
import javax.inject.Inject
import net.codingwell.scalaguice.ScalaModule

object CodingWellScalaGuice {


  trait Observable[T] {
    def now: T
  }

  class StringObservable extends Observable[String] {
    def now = "bruno"
  }

  class IntObservable extends Observable[Int] {
    def now = 42
  }

  class UseStringObs @Inject()(stringObs: Observable[String]) {
    def useIt = println("using: " + stringObs.now)
  }

  class UsePair @Inject()(pair: Observable[(String,Int)]) {
    def useIt = println("using: " + pair.now)
  }

  object ModuleA extends AbstractModule with ScalaModule {
    def configure() {
//      bind[Observable[String]].to[StringObservable]
//      bind[Observable[Int]].to[IntObservable]
      bind(new TypeLiteral[Observable[String]](){}).to(classOf[StringObservable])
      bind(new TypeLiteral[Observable[Int]](){}).to(classOf[IntObservable])
    }

    @Provides
    def stringIntPair(s: Observable[String], i: Observable[Int]) : Observable[(String,Int)] = {
      new Observable[(String,Int)] { def now = (s.now, i.now) }
    }
  }

  def main(args: Array[String]) {
    val guice = Guice.createInjector(ModuleA)
    guice.getInstance(classOf[UsePair]).useIt
    println(guice.getInstance(new Key[Observable[(String,Int)]]{}).now)


    import net.codingwell.scalaguice.InjectorExtensions._
    println( guice.instance[Observable[(String,Int)]].now ) // kaboom
  }
}
