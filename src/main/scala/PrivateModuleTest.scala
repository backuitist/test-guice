import com.google.inject.{PrivateModule, Guice, AbstractModule, Module}
import javax.inject.Inject

object PrivateModuleTest {

  trait CoreApi {
    def name : String
  }

  class CoreApiImpl extends CoreApi {
    def name = "the-core-api"
  }

  trait Dao {
    def save(name: String)
  }

  class DummyDao(path: String) extends Dao {
    def save(name: String) = println(s"Saving $name in $path")
  }

  class CoreModule extends AbstractModule {
    def configure() {
      bind(classOf[CoreApi]).to(classOf[CoreApiImpl])
    }
  }

  class UseDaoA @Inject()(dao : Dao) {
    def doIt() { dao.save("yo") }
  }

  class PrivateModuleA extends PrivateModule {
    def configure() {
      bind(classOf[Dao]).toInstance(new DummyDao("a"))
      bind(classOf[UseDaoA])
      expose(classOf[UseDaoA])
    }
  }

  class UseDaoB @Inject()(dao : Dao, coreApi: CoreApi) {
    def doIt() { dao.save("ya" + coreApi.name) }
  }

  class PrivateModuleB extends PrivateModule {
    def configure() {
      bind(classOf[Dao]).toInstance(new DummyDao("b"))
      bind(classOf[UseDaoB])
      expose(classOf[UseDaoB])
    }
  }

  def main(args: Array[String]) {
    val guice = Guice.createInjector(new CoreModule, new PrivateModuleA, new PrivateModuleB)
    guice.getInstance(classOf[UseDaoA]).doIt()
    guice.getInstance(classOf[UseDaoB]).doIt()
  }
}
