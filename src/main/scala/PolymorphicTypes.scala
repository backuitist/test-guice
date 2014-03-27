import com.google.inject._
import javax.inject.Inject

// See http://google-guice.googlecode.com/svn/trunk/javadoc/com/google/inject/TypeLiteral.html

object PolymorphicTypes {

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

  object ModuleA extends AbstractModule {
    def configure() {
      bind(new TypeLiteral[Observable[String]](){}).to(classOf[StringObservable])
      bind(new TypeLiteral[Observable[Int]](){}).to(classOf[IntObservable])
    }

    @Provides
    def stringIntPair(s: Observable[String], i: Observable[Int]) : Observable[(String,Int)] = {
      new Observable[(String,Int)] { def now = (s.now, i.now) }
    }
  }

  object PrivateModule extends PrivateModule {

    def exposePoly[T] {
      // won't work as 'T' will be found instead of the desired type!
      expose(new TypeLiteral[T](){})
    }

    def configure() {
      bind(new TypeLiteral[Observable[String]](){}).to(classOf[StringObservable])
//      bind(new TypeLiteral[Observable[Int]](){}).to(classOf[IntObservable])

      expose(new TypeLiteral[Observable[String]](){})
//      exposePoly[Observable[Int]]
    }
  }

  def main(args: Array[String]) {
    val guice = Guice.createInjector(ModuleA)
    val needClock = guice.getInstance(new Key[Observable[String]](){})
    println(needClock.now)
    println(guice.getInstance(new Key[Observable[Int]]{}).now)
    guice.getInstance(classOf[UseStringObs]).useIt
    guice.getInstance(classOf[UsePair]).useIt

    println("\n-- PRIVATE MODULE --")
    val privateGuice = Guice.createInjector(PrivateModule)
    val obs = privateGuice.getInstance(new Key[Observable[String]](){})
    println(obs.now)
//    val obsInt = privateGuice.getInstance(new Key[Observable[Int]](){})
//    println(obsInt.now)
  }
}
