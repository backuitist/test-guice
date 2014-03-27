
import com.google.inject.name.Names
import com.google.inject.{Key, Guice, PrivateModule, AbstractModule}
import com.google.inject.multibindings.Multibinder
import java.util.concurrent.atomic.AtomicInteger
import java.util.{Set=> JavaSet}
import collection.JavaConversions.asScalaSet
import javax.inject.Inject

object MultibindingWithPrivateModuleTest {

  trait Plugin

  class Application @Inject()(plugins: JavaSet[Plugin]) {
    def start() {
      println(s"Found ${plugins.size()}: ${plugins.mkString(",")}")
    }
  }

  class PluginA extends Plugin

  class ModuleA extends AbstractModule {
    def configure() {
      val multibinder = Multibinder.newSetBinder(binder(),classOf[Plugin])
      multibinder.addBinding().to(classOf[PluginA])
    }
  }

  trait SomeDep

  class PluginB @Inject()(bDep: SomeDep) extends Plugin {
    override def toString = s"PluginB[$bDep]"
  }

  class SomePrivateDep

  class SomeDepImpl @Inject()(somePrivateDep: SomePrivateDep) extends SomeDep


  class ModuleB extends AbstractModule {
    def configure() {
      val multibinder = Multibinder.newSetBinder(binder(),classOf[Plugin])
      multibinder.addBinding().to(classOf[PluginB])

      install(new PrivateModuleB)
    }
  }

  class PrivateModuleB extends PrivateModule {
    def configure() {
      bind(classOf[SomePrivateDep])
      bind(classOf[SomeDep]).to(classOf[SomeDepImpl])
      expose(classOf[SomeDep])
    }
  }


  class PluginC @Inject()(bDep: SomeDep, priv: SomePrivateDep) extends Plugin {
    override def toString = s"PluginC[$bDep]"
  }

  abstract class MultiBindingPrivateModule extends PrivateModule {
    protected def exposeMultibinding[T : Manifest, IMPL <: T : Manifest](multiBinder: Multibinder[T], idGenerator: AtomicInteger) {
      val name = manifest[T].runtimeClass.getCanonicalName + "-" + idGenerator.getAndIncrement
      val key = Key.get(manifest[Plugin].runtimeClass.asInstanceOf[Class[T]], Names.named(name))
      bind(key).to(manifest[IMPL].runtimeClass.asInstanceOf[Class[IMPL]])
      expose(key)
      multiBinder.addBinding().to(key)
    }
  }

  object AbstractModuleC {
    val pluginIdGenerator = new AtomicInteger(0)
  }

  abstract class AbstractModuleC extends AbstractModule {

    private lazy val pluginMultibinder = Multibinder.newSetBinder(binder(),classOf[Plugin])

    def configure() {
      install(configurePrivate)
    }

    def configurePrivate : AbstractPrivateModuleC

    trait AbstractPrivateModuleC extends MultiBindingPrivateModule {
      protected def bindPlugin[P <: Plugin : Manifest] {
        exposeMultibinding[Plugin, P](pluginMultibinder, AbstractModuleC.pluginIdGenerator)
      }
    }
  }

  class ModuleC extends AbstractModuleC {
    def configurePrivate = new AbstractPrivateModuleC {
      def configure() {
        bindPlugin[PluginC]
        bind(classOf[SomePrivateDep])
      }
    }
  }

  trait Plugin2
  trait SomePrivateDep2

  class SomePrivateDep2Impl extends SomePrivateDep2

  class Plugin2Impl @Inject()(priv: SomePrivateDep2) extends Plugin2

  abstract class AbstractModuleD extends AbstractModuleC {
    private val pluginIdGenerator = new AtomicInteger(0)
    private lazy val pluginMultibinder = Multibinder.newSetBinder(binder(),classOf[Plugin2])

    def configurePrivate : AbstractPrivateModuleD

    trait AbstractPrivateModuleD extends MultiBindingPrivateModule with AbstractPrivateModuleC {
      protected def bindPlugin2[P <: Plugin2 : Manifest] {
        exposeMultibinding[Plugin2, P](pluginMultibinder, pluginIdGenerator)
      }
    }
  }

  class ModuleD extends AbstractModuleD {
    def configurePrivate = new AbstractPrivateModuleD {
      def configure() {
        bindPlugin[PluginC]
        bind(classOf[SomePrivateDep])

        bind(classOf[SomePrivateDep2]).to(classOf[SomePrivateDep2Impl])
        bindPlugin2[Plugin2Impl]
      }
    }
  }

  def main(args: Array[String]) {
    Guice.createInjector(new ModuleA, new ModuleB, new ModuleC, new ModuleD).getInstance(classOf[Application]).start()
  }
}
